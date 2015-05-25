(ns combo.lib.layout
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.lib.widget :as w]))

(defn dumb-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- div [class-name contents]
  (apply dom/div #js {:className class-name} contents))

(defn- form-group [spec owner cls & contents]
  (div (str cls " " (or (om/get-state owner :group-class) (:group-class spec)))
    contents))

(defn- widget-class [spec]
  (spec :class
    (condp = (:render spec)
      w/button "btn btn-default"
      w/checkbox nil
      w/div nil
      w/a nil
      "form-control")))

(defn- widget-layout [spec]
  (spec :layout
    (fn [owner content]
      (condp = (:render spec)
        
        w/checkbox
        (form-group spec owner nil
          (dom/div #js {:className "checkbox"}
            (dom/label nil content
              (:label spec))))
        
        (form-group spec owner "form-group"
          (some->> (:label spec) (dom/label nil))
          content)))))

(defn- in-form-group [f specs]
  (map #(f (assoc % :class (widget-class %) :layout (widget-layout %)))
    specs))

(defn- in-input-group [f specs]
  (list
    (div "form-group"
      (list
        (div "input-group"
          (map #(f (assoc % :class (widget-class %)))
            specs))))))

(defn- by-input-group [[index result] spec]
  (let [g (:input-group spec)]
    (if-let [i (index g)]
      [index (update-in result [i 1] conj spec)]
      [(assoc index g (count result)) (conj result [g [spec]])])))

(defn bootstrap-layout [widget opts]
  (apply dom/form nil
    (apply concat
      (let [[_ groups] (reduce by-input-group [{} []] (:widgets opts))]
        (for [[k specs] groups]
          ((if (nil? k) in-form-group in-input-group) widget specs))))))
