(ns combo.lib.layout
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.lib.widget :as w]))

(defn dumb-layout [widget opts]
  (apply dom/div nil
    (map widget (:widgets opts))))

(defn- form-group [spec owner cls & content]
  (let [css-class (str cls " " (or (om/get-state owner :group-class)
                                   (:group-class spec)))]
    (apply dom/div #js {:className css-class} content)))

(defn- bootstrap-form-spec [spec]
  (assoc spec

    :class
    (spec :class
      (condp = (:render spec)
        w/button "btn btn-default"
        w/checkbox nil
        w/div nil
        w/a nil
        "form-control"))

    :layout
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
            content))))))

(defn bootstrap-form-layout [widget opts]
  (apply dom/form nil
    (map (comp widget bootstrap-form-spec)
      (:widgets opts))))
