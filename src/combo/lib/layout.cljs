(ns combo.lib.layout
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.lib.widget :as w]))

(defn dumb-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))

(defn- form-group [spec owner cls & content]
  (let [css-class (str cls " " (or (om/get-state owner :group-class)
                                   (:group-class spec)))]
    (apply dom/div #js {:className css-class} content)))

(defn- bootstrap-form-widget-class [spec]
  (spec :class
    (condp = (:render spec)
      w/button "btn btn-default"
      w/checkbox nil
      w/div nil
      w/a nil
      "form-control")))

(defn- bootstrap-form-widget-layout [spec]
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

(defn- extend-specs [[_ specs]]
  (for [m specs]
    (assoc m
      :class (bootstrap-form-widget-class m)
      :layout (bootstrap-form-widget-layout m))))

(defn- by-input-group [[index result] spec]
  (let [g (::input-group spec)]
    (if-let [i (index g)]
      [index (update-in result [i 1] conj spec)]
      [(assoc index g (count result)) (conj result [g [spec]])])))

(defn bootstrap-form-layout [widget opts]
  (let [[_ groups] (reduce by-input-group [{} []] (:widgets opts))
        specs (apply concat (map extend-specs groups))]
    (apply dom/form nil (map widget specs))))
