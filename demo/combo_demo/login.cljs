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

(defn- handle-username [state v]
  (let [state (assoc state :username (when (username-valid? v) v))]
    [state (state->messages state)]))

(defn- handle-password [state v]
  (let [state (assoc state :password (when-not (empty? v) v))]
    [state (state->messages state)]))

(defn- handle-submit [state]
  [state [[:alert :class "alert alert-danger"]
          [:alert :value "Login failed"]]])

(defn behavior [state event]
  (match event
    [:username :value v] (handle-username state v)
    [:password :value v] (handle-password state v)
    [:submit        _ _] (handle-submit state)
    :else [state []]))

(def units
  [{:id :alert
    :render combo/div
    :class "hidden"}
   {:id :username
    :render combo/input
    :type "email"
    :name "username"
    :placeholder "Email"
    :pretty? true}
   {:render combo/div
    :class "form-group"
    :units [{:render combo/div
             :class "input-group"
             :units [{:id :password
                      :render combo/input
                      :type "password"
                      :name "password"
                      :placeholder "Password"}
                     {:id :reset-password
                      :render combo/a
                      :class "input-group-addon"
                      :value password-reset-icon}]}]}
   {:id :submit
    :render combo/button
    :value "Sign in"
    :class "btn btn-primary btn-block"
    :disabled true
    :pretty? true}])

(defn login [_ _]
  (om/component
    (dom/div {:class "col-xs-6 col-xs-push-3"}
      (dom/div {:class "alert alert-info"}
        "Open browser console and see input and output messages.")
      (om/build combo/view nil
        {:opts {:behavior behavior
                :debug? true
                :layout combo/bootstrap-layout
                :units units}}))))
