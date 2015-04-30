(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.layout :refer [dumb-layout]]))

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
      ((:render spec) owner spec))))

(defn- widget-init-state [data spec]
  (let [v (get data (:entity spec))]
    (if (map? v)
      (select-keys v [:value :options])
      {:value v})))

(defn view [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      (let [update-chan (async/chan)]
        {:update-chan update-chan
         :update-pub  (async/pub update-chan first)
         :return-chan (async/chan)}))

    om/IWillMount
    (will-mount [_]
      (let [behavior (:behavior opts (fn [_ s] [[] s]))
            return-chan (om/get-state owner :return-chan)
            update-chan (om/get-state owner :update-chan)]
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
