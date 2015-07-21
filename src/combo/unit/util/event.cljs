(ns combo.unit.util.event)

(defn focus? [input! id yes?]
  (fn [e]
    (input! [id :focus? yes?])
    (.preventDefault e)))

(defn return-key-code
  [{:keys [id key input! filter-codes-set capture-codes-set]}]
  (fn [e]
    (let [k (.-keyCode e)
          return? (if filter-codes-set (filter-codes-set k) true)]
      (when return?
        (input! [id key k]))
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
