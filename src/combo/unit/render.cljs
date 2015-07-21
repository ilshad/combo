(ns combo.unit.render
  (:require [om.dom :as dom :include-macros true]
            [combo.unit.util.attr :as attr]))

(defn input [m]
  (dom/input
    (clj->js
      (merge (attr/field m)
             (attr/value m)
             (attr/input m)
             (attr/onkey m)
             (attr/basic m)))))

(defn textarea [m]
  (dom/textarea
    (clj->js
      (merge (attr/field m)
             (attr/value m)
             (attr/onkey m)
             (attr/basic m)))))

(defn select [m]
  (apply dom/select
    (clj->js
      (merge (attr/field m)
             (attr/value m)
             (attr/onkey m)
             (attr/basic m)))
    (for [[k v] (:options m)]
      (dom/option #js {:value k} v))))

(defn checkbox [m]
  (dom/input
    (clj->js
      (merge (attr/field m)
             (attr/check m)
             (attr/onkey m)
             (attr/basic m)))))

(defn button [m]
  (dom/button
    (clj->js
      (merge (attr/field m)
             (attr/click m)
             (attr/onkey m)
             (attr/basic m)))
    (:value m)))

(defn form [m]
  (apply dom/form
    (clj->js
      (merge (attr/form  m)
             (attr/onkey m)
             (attr/basic m)))
    (:units m)))

(defn a [m]
  (dom/a
    (clj->js
      (merge (attr/click m)
             (attr/onkey m)
             (attr/basic m)
             {:href (:href (:spec m) "#")}))
    (:value m)))

(defn span [m]
  (dom/span
    (clj->js
      (merge (attr/onkey m)
             (attr/basic m)))
    (:value m)))

(defn div [m]
  (apply dom/div
    (clj->js
      (merge (attr/click m)
             (attr/onkey m)
             (attr/basic m)))
    (if-let [value (:value m)]
      [(:value m)]
      (:units m))))
