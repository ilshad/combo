(ns combo.unit.util.event
  (:require [cljs.core.async :as async]))

(defn on-change [local-chan]
  #(async/put! local-chan (.. % -target -value)))

(defn focus? [input-chan id yes?]
  (fn [e]
    (async/put! input-chan [id :focus? yes?])
    (.preventDefault e)))

(defn return-key-code
  [{:keys [id key input-chan filter-codes-set capture-codes-set]}]
  (fn [e]
    (let [k (.-keyCode e)
          return? (if filter-codes-set (filter-codes-set k) true)]
      (when return?
        (async/put! input-chan [id key k]))
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
