(ns combo.lib.layout
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn dumb-layout [widget opts]
  (apply dom/div nil
    (map widget
      (:widgets opts))))

(defn- type-tag [spec]
  (-> spec :render meta :combo.lib.widget/type))

(defn- form-group [owner cls & content]
  (apply dom/div
    #js {:className (str cls " " (om/get-state owner :group-class))}
    content))

(defn- bootstrap-form-spec [spec]
  (assoc spec

    :class
    (spec :class
      (case (type-tag spec)
        :button   "btn btn-default"
        :checkbox nil
        :div      nil
        "form-control"))

    :layout
    (fn [owner content]
      (case (type-tag spec)

        :checkbox
        (form-group owner nil
          (dom/div #js {:className "checkbox"}
            (dom/label nil content
              (:label spec))))

        (form-group owner "form-group"
          (some->> (:label spec) (dom/label nil))
          content)))))

(defn bootstrap-form-layout [widget opts]
  (apply dom/form nil
    (map (comp widget bootstrap-form-spec)
      (:widgets opts))))
