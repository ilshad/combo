(ns examples
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.api :as combo]))

(enable-console-print!)

(defn- validate [s]
  (when (< (count s) 20)
    s))

(defn- display-result [m]
  (apply str (interpose ", " (vals m))))

(defn behavior [[entity attr value] state]
  (cond

    (= attr :value)
    (let [new-state (assoc state entity value)]
      [[[:result :value (display-result new-state)]] new-state])

    (= entity :clear)
    [[[:user   :value ""]
      [:city   :value ""]
      [:note   :value ""]
      [:agree? :value false]] {}]

    :else
    [[] state]))

(defn view [data]
  (om/build combo/view data
    {:opts {:behavior behavior
            :layout combo/bootstrap-form-layout
            :widgets [{:entity :result
                       :render combo/div}
                      {:entity :user
                       :render combo/input
                       :type "text"
                       :interceptor validate}
                      {:entity :city
                       :render combo/select}
                      {:entity :note
                       :render combo/textarea
                       :label "Note"}
                      {:entity :agree?
                       :render combo/checkbox
                       :label "Agree?"}
                      {:entity :clear
                       :render combo/button
                       :value "Clear"
                       :class #{"btn" "btn-primary" "btn-block"}}]}}))

(defn- row [content]
  (dom/div #js {:className "row"}
    (dom/hr nil)
    (dom/div #js {:className "col-xs-6 col-xs-push-3"}
      content)))

(def app-state
  (atom {:agree? false
         :city {:options {"" ""
                          "1" "New York"
                          "2" "London"
                          "3" "Tokyo"}}}))

(defn main []
  (om/root
    (fn [data _]
      (om/component
        (dom/div #js {:className "container"}
          (dom/h1 nil "Combo Examples")
          (row (view data)))))
    app-state
    {:target js/document.body}))

(set! (.-onload js/window) main)
