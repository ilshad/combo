(ns dev.core
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(enable-console-print!)

(defn- validate-user [s]
  (when (< (count s) 20)
    s))

(defn- display-result [state]
  (apply str
    (interpose "/ "
      (vals (select-keys state [:user :city :note])))))

(defn commit [args]
  (println "Received commit message from Combo:" (:message args)))

(defn extern [return _]
  (set! js/document.body.onkeydown
    (fn [e]
      (when (= e.target js/document.body)
        (return [:extern/shortcut :keycode e.keyCode])))))

(defn behavior [state event]
  (match event

    [:enable _ value]
    [state [[:note :disabled (not value)]]]
    
    [entity :value value]
    (let [state (assoc state entity value)]
      [state
       [[:result :value (display-result state)]
        [:group :class (case entity
                         :city "form-group has-error"
                         :note "form-group has-warning"
                         :user "form-group has-success")]]])

    [:clear _ _]
    [(assoc state :user "") [[:user :value ""]]]
    
    [:save _ _]
    [state [[:combo/commit :note (display-result state)]]]

    :else [state []]))

(defn view [data]
  (om/build combo/view data
    {:opts {:commit commit
            :debug? true
            :extern extern
            :behavior behavior
            :layout combo/bootstrap-layout
            :units [{:entity :group
                     :render combo/div
                     :class "form-group"
                     :units [{:entity 42
                              :render combo/div
                              :class "input-group"
                              :units [{:entity :user
                                       :render combo/input
                                       :type "text"
                                       :interceptor validate-user}
                                      {:entity :clear
                                       :render combo/a
                                       :class "input-group-addon"
                                       :value "Clear"}]}]}
                    {:entity :city
                     :pretty? true
                     :return-key-down? true
                     :capture-key-down #{13}
                     :render combo/select}
                    {:entity :enable
                     :render combo/checkbox
                     :pretty? true
                     :label "Enable note field"}
                    {:entity :note
                     :disabled true
                     :render combo/textarea
                     :pretty? true
                     :label "Note"}
                    {:entity :save
                     :render combo/button
                     :value "Save"
                     :pretty? true
                     :class "btn btn-primary btn-block"}
                    {:entity :result
                     :pretty? true
                     :render combo/div}]}}))

(defn root [data owner]
  (om/component
    (dom/div {:class "container"}
      (dom/div {:class "row"}
        (dom/div {:class "col-xs-6 col-xs-push-3"}
          (dom/br)
          (view data))))))

(def app-state
  (atom {:agree? true
         :note "Hi there"
         :city {:options {"" ""
                          "New York" "New York"
                          "London" "London"
                          "Tokyo" "Tokyo"}}}))

(defn main []
  (om/root root app-state {:target js/document.body}))

(set! (.-onload js/window) main)
