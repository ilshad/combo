(ns combo.layouts
  (:require [om.dom :as dom :include-macros true]))

(defn dumb [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- basic-field-class [opts]
  (case (-> opts :render meta :combo/tag)
    :button "btn btn-primary btn-block"
    "form-control"))

(defn basic-form [widget opts]
  (apply dom/form nil
    (for [spec (:widgets opts)]
      (dom/div #js {:className "form-group"}
        (some->> (:label i) (dom/label nil))
        (widget (assoc spec :class (basic-field-class spec)))))))
