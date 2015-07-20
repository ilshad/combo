(ns combo.unit.render
  (:require [om.dom :as dom :include-macros true]
            [combo.unit.util.attr :as attr]))

(defn input [state spec]
  (dom/input
    (clj->js
      (merge (attr/field state spec)
             (attr/value state spec)
             (attr/input state spec)
             (attr/onkey state spec)
             (attr/basic state spec)))))

(defn textarea [state spec]
  (dom/textarea
    (clj->js
      (merge (attr/field state spec)
             (attr/value state spec)
             (attr/onkey state spec)
             (attr/basic state spec)))))

(defn select [state spec]
  (apply dom/select
    (clj->js
      (merge (attr/field state spec)
             (attr/value state spec)
             (attr/onkey state spec)
             (attr/basic state spec)))
    (for [[k v] (:options state)]
      (dom/option #js {:value k} v))))

(defn checkbox [state spec]
  (dom/input
    (clj->js
      (merge (attr/field state spec)
             (attr/check state spec)
             (attr/onkey state spec)
             (attr/basic state spec)))))

(defn button [state spec]
  (dom/button
    (clj->js
      (merge (attr/field state spec)
             (attr/click state spec)
             (attr/onkey state spec)
             (attr/basic state spec)))
    (:value state)))

(defn form [state spec units]
  (apply dom/form
    (clj->js
      (merge (attr/form  state spec)
             (attr/onkey state spec)
             (attr/basic state spec)))
    units))

(defn a [state spec]
  (dom/a
    (clj->js
      (merge (attr/click state spec)
             (attr/onkey state spec)
             (attr/basic state spec)
             {:href (:href spec "#")}))
    (:value state)))

(defn span [state spec]
  (dom/span
    (clj->js
      (merge (attr/onkey state spec)
             (attr/basic state spec)))
    (:value state)))

(defn div
  ([state spec]
   (dom/div
     (clj->js
       (merge (attr/click state spec)
              (attr/onkey state spec)
              (attr/basic state spec)))
     (:value state)))

  ([state spec units]
   (apply dom/div
     (clj->js
       (merge (attr/onkey state spec)
              (attr/basic state spec)))
     units)))
