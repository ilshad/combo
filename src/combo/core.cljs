(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.layout :as layout]))

(defn widget [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      {:change-chan (async/chan)})
    
    om/IWillMount
    (will-mount [_]
      (let [change-chan (om/get-state owner :change-chan)
            return-chan (om/get-state owner :return-chan)
            handler (:handler opts identity)]
        (go-loop []
          (when-let [value (handler (async/<! change-chan))]
            (when (om/cursor? data)
              (om/update! data [:value] value))
            (async/>! return-chan [:value (:name opts) value]))
          (recur))))

    om/IRender
    (render [_]
      ((:render opts) data owner opts))))

(defn view [data owner opts]
  (reify

    om/IInitState
    (init-state [_]
      {:fields-return-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [return-chan (om/get-state owner :fields-return-chan)]
        (go-loop []
          (println (async/<! return-chan))
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
