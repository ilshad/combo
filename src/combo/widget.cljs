(ns combo.widget
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn- on-change [owner]
  (fn [e]
    (async/put! (om/get-state owner :change-chan)
      (.. e -target -value))))

(defn- focus? [yes? entity owner]
  (fn [e]
    (async/put! (om/get-state owner :return-chan) [entity :focus? yes?])
    (.preventDefault e)))

(defn- key-code-return [entity attr key-code owner]
  (fn [e]
    (when (= (.-keyCode e) key-code)
      (async/put! (om/get-state owner :return-chan) [entity attr key-code])
      (.preventDefault e))))

(defn input [owner opts]
  (dom/input
    (clj->js
      {:type        (:type opts)
       :value       (om/get-state owner :value)
       :className   (:class opts)
       :placeholder (:placeholder opts)
       :onChange    (on-change owner)
       :onFocus     (focus? true (:entity opts) owner)
       :onBlur      (focus? false (:entity opts) owner)
       :onKeyDown   (key-code-return (:entity opts) :key-down 13 owner)})))
