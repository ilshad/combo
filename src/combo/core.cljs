(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.lib.layout :refer [dumb-layout]]))

(defn- widget [_ owner spec]
  (reify
    
    om/IInitState
    (init-state [_]
      {:change-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [change-chan (om/get-state owner :change-chan)
            return-chan (om/get-state owner :return-chan)
            update-chan (om/get-state owner :update-chan)
            interceptor (:interceptor spec identity)]
        (go-loop []
          (alt!
            change-chan
            ([v]
             (let [v (interceptor v)]
               (if-not (nil? v)
                 (do (om/set-state! owner :value v)
                     (async/>! return-chan [(:entity spec) :value v])))
               (om/refresh! owner)))
            update-chan
            ([[_ attr value]]
             (om/set-state! owner attr value)))
          (recur))))

    om/IRender
    (render [_]
      (let [layout (:layout spec (fn [_ x] x))]
        (layout owner ((:render spec) owner spec))))))

(def ^:private ubiquitous-keys [:value :options :class :disabled])

(defn- widget-init-state [data spec]
  (let [props #(select-keys % ubiquitous-keys)]
    (merge
      (props spec)
      (when-let [v (data (:entity spec))]
        (if (map? v)
          (props v)
          {:value v})))))

(defn- default-commit [data owner]
  (let [intern-chan (om/get-state owner :intern-chan)
        commit-chan (om/get-state owner :commit-chan)]
    (go-loop []
      (let [[_ attr value :as msg] (async/<! intern-chan)]
        (om/update! data attr value)
        (when commit-chan
          (async/put! commit-chan msg)))
      (recur))))

(defn- setup-commit [data owner opts]
  (let [commit (:commit opts default-commit)
        intern-chan (om/get-state owner :intern-chan)
        update-pub (om/get-state owner :update-pub)]
    (async/sub update-pub :combo/commit intern-chan)
    (commit data owner)))

(defn view [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      (let [update-chan (async/chan)]
        {:update-chan update-chan
         :update-pub  (async/pub update-chan first)
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

    om/IRenderState
    (render-state [_ state]
      (let [layout (:layout opts dumb-layout)]
        (layout
          (fn [spec]
            (let [update-chan (async/chan)]
              (async/sub (:update-pub state) (:entity spec) update-chan)
              (om/build widget nil
                {:init-state (-> (widget-init-state data spec)
                                 (assoc :return-chan (:return-chan state)
                                        :update-chan update-chan))
                 :opts spec})))
          opts)))))
