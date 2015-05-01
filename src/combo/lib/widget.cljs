(ns combo.lib.widget
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn- on-change [owner]
  (fn [e]
    (async/put! (om/get-state owner :change-chan)
      (.. e -target -value))))

(defn- focus? [owner entity yes?]
  (fn [e]
    (async/put! (om/get-state owner :return-chan) [entity :focus? yes?])
    (.preventDefault e)))

(defn- key-code-return [owner entity attr key-code]
  (fn [e]
    (when (= (.-keyCode e) key-code)
      (async/put! (om/get-state owner :return-chan) [entity attr key-code])
      (.preventDefault e))))

(defn- event-keys [event]
  (reduce
    (fn [result [k v]]
      (if v
        (conj result k)
        result))
    #{}
    {:alt   (.-altKey   event)
     :ctrl  (.-ctrlKey  event)
     :meta  (.-metaKey  event)
     :shift (.-shiftKey event)}))

(defn- attrs-basic [owner spec]
  {:className (om/get-state owner :class)
   :disabled  (om/get-state owner :disabled)})

(defn- attrs-field [owner spec]
  {:value     (om/get-state owner :value)
   :onChange  (on-change owner)
   :onFocus   (focus? owner (:entity spec) true)
   :onBlur    (focus? owner (:entity spec) false)})

(defn- attrs-input [owner spec]
  {:type        (:type spec)
   :placeholder (:placeholder spec)})

(defn- attrs-checkbox [owner spec]
  {:type "checkbox"
   :checked (om/get-state owner :value)
   :onChange (fn [e]
               (async/put! (om/get-state owner :change-chan)
                 (.. e -target -checked)))})

(defn- attrs-button [owner spec]
  {:onClick (fn [e]
              (async/put! (om/get-state owner :return-chan)
                [(:entity spec) :click (event-keys e)])
              (.preventDefault e))})

(defn input [owner spec]
  (dom/input (clj->js (merge (attrs-basic owner spec)
                             (attrs-field owner spec)
                             (attrs-input owner spec)))))

(defn textarea [owner spec]
  (dom/textarea (clj->js (merge (attrs-basic owner spec)
                                (attrs-field owner spec)))))

(defn select [owner spec]
  (apply dom/select (clj->js (merge (attrs-basic owner spec)
                                    (attrs-field owner spec)))
    (for [[k v] (om/get-state owner :options)]
      (dom/option #js {:value k} v))))

(def checkbox ^{::type :checkbox}
  (fn [owner spec]
    (dom/input (clj->js (merge (attrs-basic owner spec)
                               (attrs-checkbox owner spec))))))

(def button ^{::type :button}
  (fn [owner spec]
    (dom/button (clj->js (merge (attrs-basic owner spec)
                                (attrs-button owner spec)))
      (om/get-state owner :value))))

(def div ^{::type :div}
  (fn [owner spec]
    (dom/div #js {:className (om/get-state owner :class)}
      (om/get-state owner :value))))
