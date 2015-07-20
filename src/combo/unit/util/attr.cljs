(ns combo.unit.util.attr
  (:require [cljs.core.async :as async]
            [combo.unit.util.event :as event]))

(defn basic [state spec]
  (into {:id (:element-id spec) :className (:class state)}
    (when-let [attrs (:attrs spec)]
      (attrs state spec))))

(defn field [state spec]
  {:name      (:name spec)
   :disabled  (:disabled state)})

(defn value [state spec]
  {:value     (:value state)
   :onChange  (event/on-change state)
   :onFocus   (event/focus? state (:id spec) true)
   :onBlur    (event/focus? state (:id spec) false)})

(defn input [state spec]
  {:type        (:type spec)
   :placeholder (:placeholder spec)})

(defn check [state spec]
  {:type     "checkbox"
   :checked  (:value state)
   :onChange (fn [e]
               (async/put! (:local-chan state)
                 (.. e -target -checked)))})

(defn click [state spec]
  {:onClick (fn [e]
              (async/put! (:input-chan state)
                [(:id spec) :click (event/event-keys e)])
              (.preventDefault e))})

(defn form [state spec]
  {:method (:method spec)
   :action (:action spec)
   :onSubmit (fn [e]
               (async/put! (:input-chan state)
                 [(:id spec) :submit true])
               (.preventDefault e))})

(defn onkey [state spec]
  (merge {}
    (when (:return-key-up? spec)
      {:onKeyUp
       (event/return-key-code
         {:state state
          :id (:id spec)
          :key :key-up
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-up spec #{})})})
    (when (:return-key-down? spec)
      {:onKeyDown
       (event/return-key-code
         {:state state
          :id (:id spec)
          :key :key-down
          :filter-codes-set (:filter-key-down spec)
          :capture-codes-set (:capture-key-down spec #{})})})))
