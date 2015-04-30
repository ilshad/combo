(ns combo.layout
  (:require [om.dom :as dom :include-macros true]))

(defn dumb-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- bootstrap-form-widget-class [opts]
  (case (-> opts :render meta :combo/tag)
    :button "btn btn-primary btn-block"
    "form-control"))

(defn bootstrap-form-layout [widget opts]
  (apply dom/form nil
    (for [spec (:widgets opts)]
      (dom/div #js {:className "form-group"}
        (some->> (:label spec) (dom/label nil))
        (widget (assoc spec :class (bootstrap-form-widget-class spec)))))))
