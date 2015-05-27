(ns combo.layout.bootstrap
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.unit.render :as unit]))

(defn- extend-spec [spec]
  (assoc spec

    :wrap
    (spec :wrap
      (if (:units spec)
        (fn [_ x] x)
        (fn [owner content]
          (let [cls (or (om/get-state owner :group-class) (:group-class spec))]
            (condp = (:render spec)
              
              unit/checkbox
              (dom/div #js {:className cls}
                (dom/div #js {:className "checkbox"}
                  (dom/label nil content (:label spec))))

              (dom/div #js {:className (str "form-group " cls)}
                (when-let [label (:label spec)]
                  (dom/label label))
                content))))))

    :class
    (spec :class
      (condp = (:render spec)
        unit/button "btn btn-default"
        unit/checkbox nil
        unit/span nil
        unit/div nil
        unit/a nil
        "form-control"))))

(defn bootstrap-layout [build {units :units}]
  (apply dom/div nil (map (comp build extend-spec) units)))
