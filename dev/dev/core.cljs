(ns dev.core
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [plumbing.core :refer-macros [defnk]]
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

(defn behavior [state message]
  (match message
    
    [:enable _ value]
    [state [[:note :disabled (not value)]]]
    
    [unit :value value]
    (let [state (assoc state unit value)]
      [state
       [[:result :value (display-result state)]
        [:group :class (case unit
                         :city "form-group has-error"
                         :note "form-group has-warning"
                         :user "form-group has-success")]]])

    [:clear _ _]
    [(assoc state :user "") [[:user :value ""]]]

    [:save _ _]
    [state [[:combo/commit :note (display-result state)]]]

    :else [state []]))

(defnk extern [input!]
  (set! js/document.body.onkeydown
    (fn [e]
      (when (= e.target js/document.body)
        (input! [:extern/shortcut :keycode e.keyCode])))))

(defnk commit [message]
  (println "Just received commit message:" message))

(def units
  [{:id :group
    :render combo/div
    :class "form-group"
    :units [{:render combo/div
             :class "input-group"
             :units [{:id :user
                      :render combo/input
                      :type "text"
                      :validator validate-user}
                     {:id :clear
                      :render combo/a
                      :class "input-group-addon"
                      :value "Clear"}]}]}
   {:id :city
    :pretty? true
    :return-key-down? true
    :capture-key-down #{13}
    :render combo/select}
   {:id :enable
    :render combo/checkbox
    :pretty? true
    :label "Enable note field"}
   {:id :note
    :disabled true
    :render combo/textarea
    :pretty? true
    :label "Note"}
   {:id :save
    :render combo/button
    :value "Save"
    :pretty? true
    :class "btn btn-primary btn-block"}
   {:id :result
    :pretty? true
    :render combo/div}])

(defn view [data]
  (om/build combo/view data
    {:opts {:debug? true
            :commit commit
            :extern extern
            :behavior behavior
            :layout combo/bootstrap-layout
            :units units}}))

(defn root [data _]
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
