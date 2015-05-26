(ns combo.unit.render
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.unit.util.attr :as attr]))

(defn input [owner spec]
  (dom/input (clj->js (merge (attr/basic owner spec)
                             (attr/field owner spec)
                             (attr/input owner spec)))))

(defn textarea [owner spec]
  (dom/textarea (clj->js (merge (attr/basic owner spec)
                                (attr/field owner spec)))))

(defn select [owner spec]
  (apply dom/select (clj->js (merge (attr/basic owner spec)
                                    (attr/field owner spec)))
    (for [[k v] (om/get-state owner :options)]
      (dom/option #js {:value k} v))))

(defn checkbox [owner spec]
  (dom/input (clj->js (merge (attr/basic owner spec)
                             (attr/checkbox owner spec)))))

(defn button [owner spec]
  (dom/button (clj->js (merge (attr/basic owner spec)
                              (attr/click owner spec)))
    (om/get-state owner :value)))

(defn a [owner spec]
  (dom/a (clj->js (assoc (merge (attr/basic owner spec)
                                (attr/click owner spec))
                    :href (:href spec "#")))
    (om/get-state owner :value)))

(defn span [owner spec]
  (dom/span #js {:className (om/get-state owner :class)}
    (om/get-state owner :value)))

(defn div
  ([owner spec]
   (dom/div #js {:className (om/get-state owner :class)}
     (om/get-state owner :value)))
  ([owner spec units]
   (apply dom/div #js {:className (om/get-state owner :class)}
     units)))
