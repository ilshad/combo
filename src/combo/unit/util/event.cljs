(ns combo.unit.util.event
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]))

(defn on-change [owner]
  (fn [e]
    (async/put! (om/get-state owner :local-chan)
      (.. e -target -value))))

(defn focus? [owner id yes?]
  (fn [e]
    (async/put! (om/get-state owner :input-chan) [id :focus? yes?])
    (.preventDefault e)))

(defn return-key-code
  [{:keys [owner id key filter-codes-set capture-codes-set]}]
  (fn [e]
    (let [k (.-keyCode e)
          return? (if filter-codes-set (filter-codes-set k) true)]
      (when return?
        (async/put! (om/get-state owner :input-chan) [id key k]))
      (when (capture-codes-set k)
        (.preventDefault e)))))

(defn event-keys [event]
  (reduce
    (fn [result [k v]]
      (if v (conj result k) result))
    #{} {:alt   (.-altKey   event)
         :ctrl  (.-ctrlKey  event)
         :meta  (.-metaKey  event)
         :shift (.-shiftKey event)}))
