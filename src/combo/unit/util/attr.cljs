(ns combo.unit.util.attr
  (:require [cljs.core.async :as async]
            [combo.unit.util.event :as event]))

(defn basic [{:keys [spec element-id class attrs] :as m}]
  (into {:id element-id :className class}
    (when attrs (attrs m spec))))

(defn field [{:keys [spec disabled]}]
  {:name spec
   :disabled disabled})

(defn value [{:keys [spec value local-chan input-chan]}]
  {:value value
   :onChange (event/on-change local-chan)
   :onFocus (event/focus? input-chan (:id spec) true)
   :onBlur (event/focus? input-chan (:id spec) false)})

(defn input [{:keys [spec]}]
  {:type (:type spec)
   :placeholder (:placeholder spec)})

(defn check [{:keys [value local-chan]}]
  {:type "checkbox"
   :checked  value
   :onChange #(async/put! local-chan (.. % -target -checked))})

(defn click [{:keys [spec input-chan]}]
  {:onClick (fn [e]
              (async/put! input-chan [(:id spec) :click (event/event-keys e)])
              (.preventDefault e))})

(defn form [{:keys [spec input-chan]}]
  {:method (:method spec)
   :action (:action spec)
   :onSubmit (fn [e]
               (async/put! input-chan [(:id spec) :submit true])
               (.preventDefault e))})

(defn onkey [{:keys [spec input-chan]}]
  (merge {}
    (when (:return-key-up? spec)
      {:onKeyUp
       (event/return-key-code
         {:id (:id spec)
          :key :key-up
          :input-chan input-chan
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-up spec #{})})})
    (when (:return-key-down? spec)
      {:onKeyDown
       (event/return-key-code
         {:id (:id spec)
          :key :key-down
          :input-chan input-chan
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-down spec #{})})})))
