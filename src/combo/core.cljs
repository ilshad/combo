(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.layout :as layout]))

(defn- widget [_ owner opts]
  (reify

    om/IInitState
    (init-state [_]
      {:change-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [change-chan (om/get-state owner :change-chan)
            return-chan (om/get-state owner :return-chan)
            update-chan (om/get-state owner :update-chan)
            handler (:handler opts identity)]
        (go-loop []
          (alt!
            change-chan
            ([value]
             (if-let [value (handler value)]
               (do (om/set-state! owner :value value)
                   (async/>! return-chan [(:name opts) :value value]))
               (om/refresh! owner)))
            update-chan
            ([message]
             (let [[_ topic value] message]
               (om/set-state! owner topic value))))
          (recur))))

    om/IRender
    (render [_]
      ((:render opts) owner opts))))

(defn combo [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      (let [update-chan (async/chan)]
        {:fields-update-chan update-chan
         :fields-update-pub  (async/pub update-chan first)
         :fields-return-chan (async/chan)}))

    om/IWillMount
    (will-mount [_]
      (let [manager (:manager opts (fn [_ state] [[] state]))
            return-chan (om/get-state owner :fields-return-chan)
            update-chan (om/get-state owner :fields-update-chan)]
        (go-loop [state {}]
          (let [[messages state] (manager (async/<! return-chan) state)]
            (doseq [msg messages]
              (async/>! update-chan msg)))
          (recur state))))
    
    om/IRenderState
    (render-state [_ state]
      (let [layout (:layout opts layout/dumb)
            pub (om/get-state owner :fields-update-pub)]
        (layout
          (fn [widget-opts]
            (let [update-chan (async/chan)]
              (async/sub pub (:name widget-opts) update-chan)
              (om/build widget nil
                {:init-state {:value (data (:name widget-opts))
                              :return-chan (:fields-return-chan state)
                              :update-chan update-chan}
                 :opts widget-opts})))
          opts)))))
