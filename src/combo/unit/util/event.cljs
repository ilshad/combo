(ns combo.unit.util.event
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]))

(defn on-change [owner]
  (fn [e]
    (async/put! (om/get-state owner :change-chan)
      (.. e -target -value))))

(defn focus? [owner entity yes?]
  (fn [e]
    (async/put! (om/get-state owner :return-chan) [entity :focus? yes?])
    (.preventDefault e)))

(defn return-key-code [owner entity attr]
  (fn [e]
    (async/put! (om/get-state owner :return-chan)
      [entity attr (.-keyCode e)])))

(defn capture-key-codes [owner entity attr key-codes-set]
  (fn [e]
    (let [key-code (.-keyCode e)]
      (when (key-codes-set key-code)
        (async/put! (om/get-state owner :return-chan) [entity attr key-code])
        (.preventDefault e)))))

(defn event-keys [event]
  (reduce
    (fn [result [k v]]
      (if v (conj result k) result))
    #{} {:alt   (.-altKey   event)
         :ctrl  (.-ctrlKey  event)
         :meta  (.-metaKey  event)
         :shift (.-shiftKey event)}))
