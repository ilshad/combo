(ns combo-demo.spreadsheet
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [clojure.string :as string]
            [goog.string :as gstring]
            [goog.string.format]))

(declare read-formula)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils

(defn formula? [s]
  (string? (first (re-matches #"^=.*" s))))

(defn number [x]
  (let [n (js/parseInt x)]
    (when (integer? n) n)))

(defn cell-value [state xy]
  (let [cell (state xy)]
    (if (:formula cell)
      (read-formula state (:formula cell))
      ((some-fn number identity) (:value cell)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Formula interpreter

(defn- tokenize-formula [s]
  (rest (rest (string/split s #"(=|/|\*|\+|\-)"))))

(defn- parse-formula [state s]
  (for [x (tokenize-formula s)]
    (or ({"/" / "*" * "+" + "-" -} x)
        (number x)
        (cell-value state x))))

(defn- infix [x & xs]
  (reduce (fn [res [op y]] (op res y))
    x (partition 2 xs)))

(defn read-formula [state s]
  (apply infix (parse-formula state s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Look and feel

(defn cell-mode [mode]
  (fn [xy] [[:cell xy] :class (str mode " cell col-xs-2")]))

(def edit-mode    (cell-mode "edit-mode"))
(def display-mode (cell-mode "display-mode"))
(def formula-mode (cell-mode "formula-mode"))
(def source-mode  (cell-mode "source-mode"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Behavior

(defn- format-value [v]
  (if (number? v) (gstring/format "%.2f" v) v))

(defn- calculate [state xy]
  [[:display xy] :value (format-value (cell-value state xy))])

(defn- update-dependencies [state xy]
  (map (partial calculate state) (get-in state [:dependencies xy])))

(defn- blur [state]
  (case (:mode state)
    :formula [state []]
    [(dissoc state :source)
     (into [(display-mode (:edit state))
            (calculate state (:focus state))]
       (update-dependencies state (:focus state)))]))

(defn- edit [state xy v]
  (if (formula? v)
    [(assoc state :mode :formula xy {:formula v}) [(formula-mode xy)]]
    [(assoc state :mode :value xy {:value v}) []]))

(defn- enter [state]
  [(dissoc state :mode :edit :source)
   [(display-mode (:edit state))
    (display-mode (:source state))
    (calculate state (:focus state))]])

(defn- add-dependency [xy]
  (fn [deps]
    (if (set? deps)
      (conj deps xy)
      #{xy})))

(defn- click [state xy]
  (case (:mode state)
    :formula
    (let [formula (str (get-in state [(:focus state) :formula]) xy)]
      [(-> (assoc state :source xy (:focus state) {:formula formula})
           (update-in [:dependencies xy] (add-dependency (:edit state))))
       [(source-mode xy)
        (display-mode (:source state))
        [[:edit (:focus state)] :value formula]]])
    [(assoc state :edit xy)
     [(edit-mode xy)
      (display-mode (:edit state))]]))

(defn- keydown [state k]
  (case (:mode state)
    :formula
    (if (= k 13)
      (enter state)
      (let [op ({191 "/" 189 "-" 187 "+" 56 "*"} k)
            xy (:edit state)
            v (str (:formula (state xy)) op)]
        [(assoc state xy {:formula v}) [[[:edit xy] :value v]]]))
    [state []]))

(defn behavior [state event]
  (match event
    [[:edit xy]    :focus?   true]  [(assoc state :focus xy) []]
    [[:edit _]     :focus?   false] (blur state)
    [[:edit xy]    :value    v]     (edit state xy v)
    [[:edit _]     :key-down 13]    (enter state)
    [[:display xy] :click    _]     (click state xy)
    [[:display _]  :key-down k]     (keydown state k)
    :else [state []]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec

(defn- cell [x y]
  (let [xy (str x y)]
    {:id [:cell xy]
     :render combo/div
     :class "display-mode cell col-xs-2"
     :units [{:id [:display xy]
              :return-key-down? true
              :capture-key-down #{191 189 187 56 13}
              :render combo/a}
             {:id [:edit xy]
              :render combo/input
              :type "text"
              :return-key-down? true
              :capture-key-down #{13}
              :filter-key-down #{13}}]}))

(defn- row [y]
  {:render combo/div
   :class "row"
   :units (map #(cell % y) ["A" "B" "C" "D" "E"])})

(def table
  {:render combo/div
   :units (map row (range 10))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn spreadsheet [_ _]
  (om/component
    (om/build combo/view nil
      {:opts {:behavior behavior
              :layout combo/bootstrap-layout
              :units [table]}})))
