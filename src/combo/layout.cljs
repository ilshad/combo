(ns combo.layout
  (:require [om.dom :as dom :include-macros true]))

(defn dumb-layout [widget opts]
  (apply dom/div nil
    (map widget
      (:widgets opts))))

(defn- type-tag [spec]
  (-> spec :render meta :combo.widget/type))

(defn- bootstrap-form-spec [spec]
  (assoc spec
    :class (spec :class
               (case (type-tag spec)
                 :button   #{"btn" "btn-default"}
                 :checkbox nil
                 :div      nil
                 #{"form-control"}))
    :layout (fn [owner content]
              (case (type-tag spec)
                
                :checkbox
                (dom/div #js {:className "form-group"}
                  (dom/div #js {:className "checkbox"}
                    (dom/label nil content (:label spec))))
                
                (dom/div #js {:className "form-group"}
                  (some->> (:label spec) (dom/label nil))
                  content)))))

(defn bootstrap-form-layout [widget opts]
  (apply dom/form nil
    (map (comp widget bootstrap-form-spec)
      (:widgets opts))))
