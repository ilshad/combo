(ns combo.layout
  (:require [om.dom :as dom :include-macros true]))

(defn dumb-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- bootstrap-form-widget [widget spec]
  (widget
    (assoc spec :class
      (spec :class
        (case (-> spec :render meta :combo.widget/type)
          :button "btn btn-default"
          :checkbox ""
          :div ""
          "form-control")))))

(defn- bootstrap-form-group [widget-fn spec]
  (let [widget (bootstrap-form-widget widget-fn spec)]
    (case (-> spec :render meta :combo.widget/type)
      
      :checkbox
      (dom/div #js {:className "form-group"}
        (dom/div #js {:className "checkbox"}
          (dom/label nil widget (:label spec))))

      (dom/div #js {:className "form-group"}
        (some->> (:label spec) (dom/label nil))
        widget))))

(defn bootstrap-form-layout [widget opts]
  (apply dom/form nil
    (map (partial bootstrap-form-group widget)
      (:widgets opts))))
