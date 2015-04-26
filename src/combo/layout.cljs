(ns combo.layout
  (:require [om.dom :as dom :include-macros true]))

(defn dumb [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- basic-field-class [opts]
  (case (-> opts :render meta :spec/tag)
    :button "btn btn-primary btn-block"
    "form-control"))

(defn- basic-field-opts [opts]
  (merge
    {:class (basic-field-class opts)}
    opts))

(defn basic-form [widget opts]
  (apply dom/form nil
    (for [i (:widgets opts)]
      (dom/div #js {:className "form-group"}
        (some->> (:label i) (dom/label nil))
        (widget (basic-field-opts i))))))
