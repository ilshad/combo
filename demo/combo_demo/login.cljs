(ns combo-demo.login
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(def forgot-icon
  (dom/i {:class "fa fa-question-circle fa-lg"}))

(defn- username-valid? [s]
  (boolean (re-matches #"\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*" s)))

(defn- allow-login? [state]
  (and (:username state) (:password state)))

(defn- messages [state]
  [[:submit :disabled (not (allow-login? state))]])

(defn- handle-username [v state]
  (let [state (assoc state :username (when (username-valid? v) v))]
    [(messages state) state]))

(defn- handle-password [v state]
  (let [state (assoc state :password (when-not (empty? v) v))]
    [(messages state) state]))

(defn behavior [message state]
  (match message
    [:username :value v] (handle-username v state)
    [:password :value v] (handle-password v state)
    :else [[] state]))

(defn login [app owner]
  (om/component
    (dom/div {:class "col-xs-6 col-xs-push-3"}
      (om/build combo/view nil
        {:opts {:behavior behavior
                :debug? true
                :layout combo/bootstrap-layout
                :units [{:entity :alert
                         :render combo/div
                         :class "hidden"}
                        {:entity :username
                         :render combo/input
                         :type "email"
                         :name "username"
                         :placeholder "Email"
                         :pretty? true}
                        {:render combo/div
                         :class "form-group"
                         :units [{:render combo/div
                                  :class "input-group"
                                  :units [{:entity :password
                                           :render combo/input
                                           :type "password"
                                           :name "password"
                                           :placeholder "Password"}
                                          {:entity :reset-password
                                           :render combo/a
                                           :class "input-group-addon"
                                           :value forgot-icon}]}]}
                        {:entity :submit
                         :render combo/button
                         :value "Sign in"
                         :class "btn btn-primary btn-block"
                         :disabled true
                         :pretty? true}]}}))))
