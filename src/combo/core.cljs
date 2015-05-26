(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [combo.layouts.simple :refer [simple-layout]]))

(defn- default-commit [data owner]
  (let [in (om/get-state owner :intern-chan)
        out (om/get-state owner :commit-chan)]
    (go-loop []
      (let [[_ a v :as msg] (async/<! in)]
        (when data (om/update! data a v))
        (when out (async/>! out msg)))
      (recur))))

(defn- setup-commit [data owner opts]
  (let [commit (:commit opts default-commit)
        pubc (om/get-state owner :update-pubc)
        chan (om/get-state owner :intern-chan)]
    (async/sub pubc :combo/commit chan)
    (when data (commit data owner))))

(defn- widget-init-state [data spec]
  (let [props #(select-keys % [:value :options :class :disabled])]
    (merge
      (props spec)
      (when-let [v (get data (:entity spec))]
        (if (map? v)
          (props v)
          {:value v})))))

(defn- widget-params [data owner spec]
  {:init-state (-> (widget-init-state data spec)
                   (assoc :update-pubc (om/get-state owner :update-pubc)
                          :return-chan (om/get-state owner :return-chan)))
   :opts spec})

(defn- widget [data owner spec]
  (reify
    
    om/IInitState
    (init-state [_]
      {:change-chan (async/chan)
       :update-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [interceptor (:interceptor spec identity)
            change-chan (om/get-state owner :change-chan)
            return-chan (om/get-state owner :return-chan)
            update-chan (om/get-state owner :update-chan)
            update-pubc (om/get-state owner :update-pubc)]
        (async/sub update-pubc (:entity spec) update-chan)
        (go-loop []
          (alt!
            change-chan
            ([v]
             (let [v (interceptor v)]
               (when-not (nil? v)
                 (om/set-state! owner :value v)
                 (async/>! return-chan [(:entity spec) :value v]))
               (om/refresh! owner)))
            update-chan
            ([[_ attr value]]
             (om/set-state! owner attr value)))
          (recur))))

    om/IRender
    (render [_]
      (let [layout (:layout spec (fn [_ x] x))]
        (layout owner
          (apply (:render spec) owner spec
            (when-let [specs (:widgets spec)]
              (list
                (map
                  (fn [child]
                    (om/build widget (if (:widgets child) data nil)
                      (widget-params data owner child)))
                  specs)))))))))

(defn view [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      (let [update-chan (async/chan)]
        {:update-chan update-chan
         :update-pubc (async/pub update-chan first)
         :return-chan (async/chan)
         :intern-chan (async/chan)}))

    om/IWillMount
    (will-mount [_]
      (let [behavior (:behavior opts (fn [_ s] [[] s]))
            return-chan (om/get-state owner :return-chan)
            update-chan (om/get-state owner :update-chan)]
        (setup-commit data owner opts)
        (go-loop [state {}]
          (let [[messages new-state] (behavior (async/<! return-chan) state)]
            (doseq [m messages]
              (async/>! update-chan m))
            (recur new-state)))))

    om/IRender
    (render [_]
      (let [layout (:layout opts simple-layout)]
        (layout
          (fn [spec]
            (om/build widget (if (:widgets spec) data nil)
              (widget-params data owner spec)))
          opts)))))
