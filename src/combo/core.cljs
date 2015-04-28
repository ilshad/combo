(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
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
            (om/update! data [:value] value)
            (async/>! return-chan [:value (:name opts) value]))
          (recur))))

    om/IRender
    (render [_]
      ((:render opts) data owner opts))))

(defn view [data owner opts]
  (om/component
    (let [layout (:layout opts layout/dumb)]
      (layout
        (fn [{:keys [name]}]
          (om/build widget (data name)))
        opts))))
