(ns examples
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.core :refer [combo]]
            [combo.layouts :as layouts]
            [combo.widgets :as widgets]))

(enable-console-print!)

(defn- validate [s]
  (when (< (count s) 10)
    s))

(defn behavior [[entity attr value] state]
  (println entity attr value state)
  (case attr
    :value [[] (assoc state entity value)]
    [[] state]))

(defn view [data]
  (om/build combo data
    {:opts {:layout layouts/basic-form
            :behavior behavior
            :widgets [{:entity :username
                       :render widgets/input
                       :type "text"
                       :handler validate}
                      {:entity :password
                       :render widgets/input
                       :type "password"}]}}))

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
          (row (view data)))))
    (atom {})
    {:target js/document.body}))

(set! (.-onload js/window) main)
