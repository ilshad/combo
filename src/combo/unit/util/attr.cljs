(ns combo.unit.util.attr
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [combo.unit.util.event :as event]))

(defn basic [owner spec]
  (into {:id (:element-id spec)
         :className (om/get-state owner :class)}
    (when-let [attrs (:attrs spec)]
      (attrs owner spec))))

(defn field [owner spec]
  {:name      (:name spec)
   :disabled  (om/get-state owner :disabled)})

(defn value [owner spec]
  {:value     (om/get-state owner :value)
   :onChange  (event/on-change owner)
   :onFocus   (event/focus? owner (:id spec) true)
   :onBlur    (event/focus? owner (:id spec) false)})

(defn input [owner spec]
  {:type        (:type spec)
   :placeholder (:placeholder spec)})

(defn check [owner spec]
  {:type     "checkbox"
   :checked  (om/get-state owner :value)
   :onChange (fn [e]
               (async/put! (om/get-state owner :local-chan)
                 (.. e -target -checked)))})

(defn click [owner spec]
  {:onClick (fn [e]
              (async/put! (om/get-state owner :input-chan)
                [(:id spec) :click (event/event-keys e)])
              (.preventDefault e))})

(defn form [owner spec]
  {:method (:method spec)
   :action (:action spec)
   :onSubmit (fn [e]
               (async/put! (om/get-state owner :input-chan)
                 [(:id spec) :submit true])
               (.preventDefault e))})

(defn onkey [owner spec]
  (merge {}
    (when (:return-key-up? spec)
      {:onKeyUp
       (event/return-key-code
         {:owner owner
          :id (:id spec)
          :key :key-up
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-up spec #{})})})
    (when (:return-key-down? spec)
      {:onKeyDown
       (event/return-key-code
         {:owner owner
          :id (:id spec)
          :key :key-down
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-down spec #{})})})))
