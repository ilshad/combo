(ns examples
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.core :as combo]
            [combo.widget :as widget]))

(enable-console-print!)

(defn widgets [data owner]
  (reify

    om/IInitState
    (init-state [_]
      {:fields-return-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [c (om/get-state owner :fields-return-chan)]
        (go
          (while true
            (println "Message from widget:" (async/<! c))))))
    
    om/IRender
    (render [_]
      (om/build combo/widget data
        {:init-state {:return-chan (om/get-state owner :fields-return-chan)}
         :opts {:name :username
                :render widget/input
                :type "text"
                :class "form-control"}}))))

(defn- row [content]
  (dom/div #js {:className "row"}
    (dom/hr nil)
    (dom/div #js {:className "col-xs-6 col-xs-push-3"}
      content)))

(defn main []
  (om/root
    (fn [data _]
      (om/component
        (dom/div #js {:className "container"}
          (dom/h1 nil "Combo Examples")
          (row (om/build widgets data)))))
    (atom {})
    {:target js/document.body}))

(set! (.-onload js/window) main)
