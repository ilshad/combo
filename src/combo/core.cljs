(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.layout :as layout]))

(defn- widget [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      {:change-chan (async/chan)
       :value (:value data)})

    om/IWillMount
    (will-mount [_]
      (let [change-chan (om/get-state owner :change-chan)
            return-chan (om/get-state owner :return-chan)
            handler (:handler opts identity)]
        (go-loop []
          (if-let [value (handler (async/<! change-chan))]
            (do (async/>! return-chan [:value (:name opts) value])
                (om/set-state! owner :value value)
                (when (om/cursor? data) (om/update! data :value value)))
            (om/refresh! owner))
          (recur))))

    om/IRender
    (render [_]
      ((:render opts) data owner opts))))

(defn combo [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      {:fields-return-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [return-chan (om/get-state owner :fields-return-chan)]
        (go-loop []
          (println (async/<! return-chan) data)
          (recur))))
    
    om/IRenderState
    (render-state [_ state]
      (let [layout (:layout opts layout/dumb)]
        (layout
          (fn [widget-opts]
            (om/build widget (data (:name widget-opts))
              {:init-state {:return-chan (:fields-return-chan state)}
               :opts widget-opts}))
          opts)))))
