(ns combo.unit.util.event
  (:require [cljs.core.async :as async]))

(defn on-change [state]
  (fn [e]
    (async/put! (:local-chan state)
      (.. e -target -value))))

(defn focus? [state id yes?]
  (fn [e]
    (async/put! (:input-chan state) [id :focus? yes?])
    (.preventDefault e)))

(defn return-key-code
  [{:keys [state id key filter-codes-set capture-codes-set]}]
  (fn [e]
    (let [k (.-keyCode e)
          return? (if filter-codes-set (filter-codes-set k) true)]
      (when return?
        (async/put! (:input-chan state) [id key k]))
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
