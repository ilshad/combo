(ns examples.spreadsheet
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(defn cell-mode [mode x y]
  [[:cell x y] :class (str mode " cell col-xs-2")])

(def edit-mode    (partial cell-mode "edit-mode"))
(def display-mode (partial cell-mode "display-mode"))
(def formula-mode (partial cell-mode "formula-mode"))
(def source-mode  (partial cell-mode "source-mode"))

(defn formula? [v] (string? (first (re-matches #"^=.*" v))))

(defn behavior [message state]
  (println message "::" state)
  (match message

    [[:display x y] :click _]
    (if (= (:mode state) :formula)
      (let [formula (str (state (:focus state)) [x y])
            [fx fy] (:focus state)]
        [[(source-mode x y) [[:edit fx fy] :value formula]]
         (assoc state (:focus state) formula)])
      [[(edit-mode x y)] state])

    [[:edit x y] :focus? true]
    [[] (assoc state :focus [x y])]
    
    [[:edit x y] :focus? false]
    (if-not (= (:mode state) :formula)
      [[(display-mode x y)] state]
      [[] state])

    [[:edit x y] :value v]
    (if (formula? v)
      [[(formula-mode x y)] (assoc state :mode :formula [x y] v)]
      [[] (-> (dissoc state :mode :formula) (assoc [x y] v))])
    
    :else [[] state]))

(defn cell [x y]
  {:entity [:cell x y]
   :render combo/div
   :class "display-mode cell col-xs-2"
   :units [{:entity [:display x y] :render combo/a     :on-key-down 13}
           {:entity [:edit x y]    :render combo/input :type "text"}]})

(defn row [y width]
  {:entity [:row y]
   :render combo/div
   :class "row"
   :units (map #(cell % y) [:a :b :c :d :e])})

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
