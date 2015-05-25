(ns combo.widgets.utils.attrs
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [combo.widgets.utils.event :as event]))

(defn basic [owner spec]
  {:className (om/get-state owner :class)
   :disabled  (om/get-state owner :disabled)})

(defn field [owner spec]
  {:value     (om/get-state owner :value)
   :name      (:name spec)
   :onChange  (event/on-change owner)
   :onFocus   (event/focus? owner (:entity spec) true)
   :onBlur    (event/focus? owner (:entity spec) false)})

(defn input [owner spec]
  {:type        (:type spec)
   :placeholder (:placeholder spec)})

(defn checkbox [owner spec]
  {:type     "checkbox"
   :checked  (om/get-state owner :value)
   :onChange (fn [e]
               (async/put! (om/get-state owner :change-chan)
                 (.. e -target -checked)))})

(defn click [owner spec]
  {:onClick (fn [e]
              (async/put! (om/get-state owner :return-chan)
                [(:entity spec) :click (event/event-keys e)])
              (.preventDefault e))})
