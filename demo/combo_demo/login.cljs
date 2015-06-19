(ns combo-demo.login
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(def password-reset-icon
  (dom/i {:class "fa fa-question-circle fa-lg"}))

(defn- username-valid? [s]
  (boolean (re-matches #"\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*" s)))

(defn- allow-login? [state]
  (and (:username state) (:password state)))

(defn- state->messages [state]
  [[:submit :disabled (not (allow-login? state))]])

(defn- handle-username [v state]
  (let [state (assoc state :username (when (username-valid? v) v))]
    [(state->messages state) state]))

(defn- handle-password [v state]
  (let [state (assoc state :password (when-not (empty? v) v))]
    [(state->messages state) state]))

(defn- handle-submit [state]
  [[[:alert :class "alert alert-danger"]
    [:alert :value "Login failed"]]
   state])

(defn behavior [message state]
  (match message
    [:username :value v] (handle-username v state)
    [:password :value v] (handle-password v state)
    [:submit        _ _] (handle-submit state)
    :else [[] state]))

(defn login [app owner]
  (om/component
    (dom/div {:class "col-xs-6 col-xs-push-3"}
      (dom/div {:class "alert alert-info"}
        "Open browser console and see input and output messages.")
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
                                           :value password-reset-icon}]}]}
                        {:entity :submit
                         :render combo/button
                         :value "Sign in"
                         :class "btn btn-primary btn-block"
                         :disabled true
                         :pretty? true}]}}))))
