(ns combo.layouts.bootstrap
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.widgets.render :as widget]))

(defn- widget-class [spec]
  (spec :class
    (condp = (:render spec)
      widget/button "btn btn-default"
      widget/checkbox nil
      widget/div nil
      widget/a nil
      "form-control")))

(defn- group-class [class-name owner spec]
  (str class-name " "
       (or (om/get-state owner :group-class)
           (:group-class spec))))

(defn- div [class-name content & contents]
  (apply dom/div #js {:className class-name} content contents))

(defn- widget-layout [spec]
  (spec :layout
    (fn [owner content]
      (condp = (:render spec)
        
        widget/checkbox
        (div (group-class "" owner spec)
          (div "checkbox"
            (dom/label nil content
              (:label spec))))
        
        (div (group-class "form-group" owner spec)
          (some->> (:label spec) (dom/label nil))
          content)))))

(defn- map-form-group [f specs]
  (map #(f (assoc % :class (widget-class %) :layout (widget-layout %)))
    specs))

(defn- map-input-group [f specs]
  (list
    (div "form-group"
      (apply dom/div #js {:className "input-group"}
        (map #(f (assoc % :class (widget-class %)))
          specs)))))

(defn- by-input-group [[index result] spec]
  (let [g (:group spec)]
    (if-let [i (index g)]
      [index (update-in result [i 1] conj spec)]
      [(assoc index g (count result)) (conj result [g [spec]])])))

(defn bootstrap-layout [widget opts]
  (apply div nil nil
    (apply concat
      (let [[_ groups] (reduce by-input-group [{} []] (:widgets opts))]
        (for [[k specs] groups]
          ((if (nil? k) map-form-group map-input-group) widget specs))))))
