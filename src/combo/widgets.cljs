(ns combo.widgets
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

(defn- attrs-basic [owner opts]
  {:value     (om/get-state owner :value)
   :className (:class opts)
   :onChange  (on-change owner)
   :onFocus   (focus? true  (:entity opts) owner)
   :onBlur    (focus? false (:entity opts) owner)})

(defn- attrs-input [owner opts]
  {:type        (:type opts)
   :placeholder (:placeholder opts)})

(defn input [owner opts]
  (dom/input (clj->js (merge (attrs-basic owner opts)
                             (attrs-input owner opts)))))

(defn select [owner opts]
  (apply dom/select (clj->js (attrs-basic owner opts))
    (for [[k v] (om/get-state owner :options)]
      (dom/option #js {:value k} v))))
