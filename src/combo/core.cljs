(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [combo.layout.simple :refer [simple-layout]]))

(declare unit)

(defn- default-commit [data owner]
  (let [in (om/get-state owner :intern-chan)
        out (om/get-state owner :commit-chan)]
    (go-loop []
      (let [[_ a v :as msg] (async/<! in)]
        (when data (om/update! data a v))
        (when out (async/>! out msg)))
      (recur))))

(defn- setup-commit [data owner spec]
  (let [commit (:commit spec default-commit)
        pubc (om/get-state owner :update-pubc)
        chan (om/get-state owner :intern-chan)]
    (async/sub pubc :combo/commit chan)
    (when data (commit data owner))))

(defn- setup-behavior [owner spec]
  (let [behavior (:behavior spec (fn [_ s] [[] s]))
        return-chan (om/get-state owner :return-chan)
        update-chan (om/get-state owner :update-chan)]
    (go-loop [state {}]
      (let [[messages new-state] (behavior (async/<! return-chan) state)]
        (doseq [m messages] (async/>! update-chan m))
        (recur new-state)))))

(defn- unit-init-state [data spec]
  (let [props #(select-keys % [:value :options :class :disabled])]
    (merge
      (props spec)
      (when-let [v (get data (:entity spec))]
        (if (map? v)
          (props v)
          {:value v})))))

(defn- unit-params [data owner spec layout]
  {:init-state (assoc (unit-init-state data spec)
                 :update-pubc (om/get-state owner :update-pubc)
                 :return-chan (om/get-state owner :return-chan))
   :opts (if (:units spec) (assoc spec :layout layout) spec)})

(defn- build [data owner layout]
  (fn [spec]
    (om/build unit data
      (unit-params data owner spec layout))))

(defn- nested [data owner spec]
  (when-let [specs (:units spec)]
    (list (map (build data owner (:layout spec)) specs))))

(defn- spec->dom [data owner]
  (fn [spec]
    (let [wrap (:wrap spec (fn [owner content] content))
          units (nested data owner spec)
          content (apply (:render spec) owner spec units)]
      (wrap owner content))))

(defn- unit [data owner spec]
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
      (let [f (spec->dom data owner)]
        (if-let [layout (:layout spec)]
          (layout f spec)
          (f spec))))))

(defn view [data owner spec]
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
      (setup-commit data owner spec)
      (setup-behavior owner spec))

    om/IRender
    (render [_]
      (let [layout (:layout spec simple-layout)]
        (layout (build data owner layout) spec)))))
