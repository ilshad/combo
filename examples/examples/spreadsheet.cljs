(ns examples.spreadsheet
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(defn edit-mode    [x y] [[:cell x y] :class "edit-mode    cell col-xs-2"])
(defn display-mode [x y] [[:cell x y] :class "display-mode cell col-xs-2"])
(defn formula-mode [x y] [[:cell x y] :class "formula-mode cell col-xs-2"])

(defn formula? [v] (string? (first (re-matches #"^=.*" v))))

(defn behavior [message state]
  (println message state)
  (match message

    [[:show x y] :click _] [[(edit-mode x y)] state]

    [[:edit x y] :value v]
    (if (formula? v)
      [[(formula-mode x y)] (assoc state :mode :formula [x y] v)]
      [[] (-> state (dissoc :mode :formula) (assoc [x y] v))])

    [[:edit x y] :focus? false] [[(display-mode x y)] state]
    :else [[] state]))

(defn cell [x y]
  {:entity [:cell x y]
   :render combo/div
   :class "display-mode cell col-xs-2"
   :units [{:entity [:show x y] :render combo/a}
           {:entity [:edit x y] :render combo/input :type "text"}]})

(defn row [y width]
  {:entity [:row y]
   :render combo/div
   :class "row"
   :units (map #(cell % y) [:a :b :c :d :e :f])})

(defn table []
  {:entity :table
   :render combo/div
   :units (map row (range 10))})

(defn spreadsheet [app owner]
  (om/component
    (om/build combo/view nil
      {:opts {:behavior behavior
              :layout combo/bootstrap-layout
              :units [(table)]}})))
