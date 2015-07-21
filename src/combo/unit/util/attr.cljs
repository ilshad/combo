(ns combo.unit.util.attr
  (:require [combo.unit.util.event :as event]))

(defn basic [{:keys [spec element-id class attrs] :as m}]
  (into {:id element-id :className class}
    (when attrs (attrs m spec))))

(defn field [{:keys [spec disabled]}]
  {:name spec
   :disabled disabled})

(defn value [{:keys [spec value local! input!]}]
  {:value value
   :onChange #(local! (.. % -target -value))
   :onFocus (event/focus? input! (:id spec) true)
   :onBlur (event/focus? input! (:id spec) false)})

(defn input [{:keys [spec]}]
  {:type (:type spec)
   :placeholder (:placeholder spec)})

(defn check [{:keys [value local!]}]
  {:type "checkbox"
   :checked  value
   :onChange #(local! (.. % -target -checked))})

(defn click [{:keys [spec input!]}]
  {:onClick (fn [e]
              (input! [(:id spec) :click (event/event-keys e)])
              (.preventDefault e))})

(defn form [{:keys [spec input!]}]
  {:method (:method spec)
   :action (:action spec)
   :onSubmit (fn [e]
               (input! [(:id spec) :submit true])
               (.preventDefault e))})

(defn onkey [{:keys [spec input!]}]
  (merge {}
    (when (:return-key-up? spec)
      {:onKeyUp
       (event/return-key-code
         {:id (:id spec)
          :key :key-up
          :input! input!
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-up spec #{})})})
    (when (:return-key-down? spec)
      {:onKeyDown
       (event/return-key-code
         {:id (:id spec)
          :key :key-down
          :input! input!
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-down spec #{})})})))
