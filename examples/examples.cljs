(ns examples
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.api :as combo]))

(enable-console-print!)

(defn- validate-user [s]
  (when (< (count s) 20)
    s))

(defn- display-result [state]
  (apply str
    (interpose ", "
      (vals (select-keys state [:user :city :note])))))

(defn behavior [[entity attr value] state]
  (cond
    
    (= entity :clear)
    [[[:user   :value ""]
      [:city   :value ""]
      [:note   :value ""]
      [:result :value (str "It was " (display-result state))]
      [:clear  :value "OK"]
      [:clear  :class "btn btn-primary btn-block"]] {}]

    (= entity :enable)
    [[[:note :disabled (not value)]] state]
    
    (= attr :value)
    (let [new-state (assoc state entity value)]
      [[[:result :value (display-result new-state)]
        [:clear  :value "Clear"]
        [:clear  :class "btn btn-warning btn-block"]] new-state])

    :else
    [[] state]))

(defn view [data]
  (om/build combo/view data
    {:opts {:behavior behavior
            :layout combo/bootstrap-form-layout
            :widgets [{:entity :user
                       :render combo/input
                       :type "text"
                       :interceptor validate-user}
                      {:entity :city
                       :render combo/select}
                      {:entity :enable
                       :render combo/checkbox
                       :label "Enable note field"}
                      {:entity :note
                       :disabled true
                       :render combo/textarea
                       :label "Note"}
                      {:entity :clear
                       :render combo/button
                       :value "LOL"
                       :class "btn btn-warning btn-block"}
                      {:entity :result
                       :render combo/div}]}}))

(def app-state
  (atom {:agree? true
         :city {:value "London"
                :options {"" ""
                          "New York" "New York"
                          "London" "London"
                          "Tolyo" "Tokyo"}}}))

(defn main []
  (om/root
    (fn [data _]
      (om/component
        (dom/div #js {:className "container"}
          (dom/div #js {:className "row"}
            (dom/div #js {:className "col-xs-6 col-xs-push-3"}
              (dom/br nil)
              (view data))))))
    app-state
    {:target js/document.body}))

(set! (.-onload js/window) main)
