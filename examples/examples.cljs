(ns examples
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.core :as combo]))

(enable-console-print!)

(defn- row [content]
  (dom/div #js {:className "row"}
    (dom/hr nil)
    (dom/div #js {:className "col-xs-6 col-xs-push-3"}
      content)))

(defn main []
  (om/root
    (fn [app _]
      (om/component
        (dom/div #js {:className "container"}
          (dom/h1 nil "Combo Examples"))))
    (atom {})
    {:target js/document.body}))

(set! (.-onload js/window) main)
