(ns combo.widget
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn- on-change [owner]
  (fn [e]
    (async/put! (om/get-state owner :change-chan)
      (.. e -target -value))))

(defn- focus? [yes? name owner]
  (fn [e]
    (async/put! (om/get-state owner :return-chan) [name :focus? yes?])
    (.preventDefault e)))

(defn- key-code-return [name topic key-code owner]
  (fn [e]
    (when (= (.-keyCode e) key-code)
      (async/put! (om/get-state owner :return-chan) [name topic key-code])
      (.preventDefault e))))

(defn input [owner opts]
  (let [name (:name opts)]
    (dom/input
      (clj->js
        {:type        (:type opts)
         :value       (om/get-state owner :value)
         :className   (:class opts)
         :placeholder (:placeholder opts)
         :onChange    (on-change owner)
         :onFocus     (focus? true name owner)
         :onBlur      (focus? false name owner)
         :onKeyDown   (key-code-return name :key-down 13 owner)}))))
