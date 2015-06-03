(ns combo.unit.render
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.unit.util.attr :as attr]))

(defn input [owner spec]
  (dom/input
    (clj->js
      (merge (attr/basic owner spec)
             (attr/field owner spec)
             (attr/value owner spec)
             (attr/input owner spec)
             (attr/onkey owner spec)))))

(defn textarea [owner spec]
  (dom/textarea
    (clj->js
      (merge (attr/basic owner spec)
             (attr/field owner spec)
             (attr/value owner spec)))))

(defn select [owner spec]
  (apply dom/select
    (clj->js
      (merge (attr/basic owner spec)
             (attr/field owner spec)
             (attr/value owner spec)))
    (for [[k v] (om/get-state owner :options)]
      (dom/option #js {:value k} v))))

(defn checkbox [owner spec]
  (dom/input
    (clj->js
      (merge (attr/basic owner spec)
             (attr/field owner spec)
             (attr/check owner spec)))))

(defn button [owner spec]
  (dom/button
    (clj->js
      (merge (attr/basic owner spec)
             (attr/field owner spec)
             (attr/click owner spec)))
    (om/get-state owner :value)))

(defn form [owner spec units]
  (apply dom/form
    (clj->js
      (merge (attr/basic owner spec)
             (attr/form  owner spec)))
    units))

(defn a [owner spec]
  (dom/a
    (clj->js
      (merge (attr/basic owner spec)
             (attr/click owner spec)
             {:href (:href spec "#")}))
    (om/get-state owner :value)))

(defn span [owner spec]
  (dom/span (clj->js (attr/basic owner spec))
    (om/get-state owner :value)))

(defn div
  ([owner spec]
   (dom/div (clj->js (attr/basic owner spec))
     (om/get-state owner :value)))

  ([owner spec units]
   (apply dom/div (clj->js (attr/basic owner spec))
     units)))
