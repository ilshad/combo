(ns examples.spreadsheet
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(defn cell-mode [mode]
  (fn [xy]
    [[:cell xy] :class (str mode " cell col-xs-2")]))

(def edit-mode    (cell-mode "edit-mode"))
(def display-mode (cell-mode "display-mode"))
(def formula-mode (cell-mode "formula-mode"))
(def source-mode  (cell-mode "source-mode"))

(defn formula? [v]
  (string? (first (re-matches #"^=.*" v))))

(defn behavior [message state]
  (println message "::" state)
  (match message

    [[:display xy] :click _]
    (if (= (:mode state) :formula)
      (let [formula (str (state (:focus state)) xy)]
        [[(source-mode xy) [[:edit (:focus state)] :value formula]]
         (assoc state (:focus state) formula)])
      [[(edit-mode xy) (display-mode (:edit state))]
       (assoc state :edit xy)])
    
    [[:edit xy] :focus? true]
    [[] (assoc state :focus xy)]
    
    [[:edit xy] :focus? false]
    (if (= (:mode state) :formula)
      [[] state]
      [[(display-mode xy) [[:display xy] :value (state (:focus state))]]
       (assoc state :mode :done)])

    [[:edit xy] :value v]
    (if (formula? v)
      [[(formula-mode xy)] (assoc state :mode :formula xy v)]
      [[] (assoc state :mode :value xy v)])

    [[:edit xy] :key-down 13]
    [[(display-mode xy) [[:display xy] :value (state (:focus state))]]
     (assoc state :mode :done)]
    
    :else [[] state]))

(defn cell [x y]
  (let [xy (str x y)]
    {:entity [:cell xy]
     :render combo/div
     :class "display-mode cell col-xs-2"
     :units [{:entity [:display xy]
              :render combo/a}
             {:entity [:edit xy]
              :render combo/input
              :type "text"
              :capture-key-down #{13}}]}))

(defn row [y width]
  {:entity [:row y]
   :render combo/div
   :class "row"
   :units (map #(cell % y) ["A" "B" "C" "D" "E" "F"])})

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
