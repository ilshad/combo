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
    (async/put! (om/get-state owner :return-chan) [:focus? name yes?])
    (.preventDefault e)))

(defn- key-code-return [topic name key-code owner]
  (fn [e]
    (when (= (.-keyCode e) key-code)
      (async/put! (om/get-state owner :return-chan) [topic name key-code])
      (.preventDefault e))))

(defn input [data owner opts]
  (let [name (:name opts)]
    (dom/input
      (clj->js
        {:type        (:type opts)
         :value       (:value data)
         :className   (:class opts)
         :placeholder (:placeholder opts)
         :onChange    (on-change owner)
         :onFocus     (focus? true name owner)
         :onBlur      (focus? false name owner)
         :onKeyDown   (key-code-return :key-down name 13 owner)}))))
