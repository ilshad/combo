(ns combo.layout
  (:require [om.dom :as dom :include-macros true]))

(defn dumb-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- bootstrap-form-widget [widget spec]
  (widget
    (assoc spec :class
      (spec :class
        (condp #(contains? %2 %1) (-> spec :render meta :tags)
          :button "btn btn-default"
          :checkbox ""
          "form-control")))))

(defn- bootstrap-form-group [widget-fn spec]
  (let [widget (bootstrap-form-widget widget-fn spec)
        group (fn [& args] (apply dom/div #js {:className "form-group"} args))]
    (condp #(contains? %2 %1) (-> spec :render meta :tags)
      
      :checkbox
      (group
        (dom/div #js {:className "checkbox"}
          (dom/label nil widget (:label spec))))

      (group
        (some->> (:label spec) (dom/label nil))
        widget))))

(defn bootstrap-form-layout [widget opts]
  (apply dom/form nil
    (map (partial bootstrap-form-group widget)
      (:widgets opts))))
