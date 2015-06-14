(ns examples.spreadsheet
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [clojure.string :as string]))

(declare read-formula)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

(defn formula? [s]
  (string? (first (re-matches #"^=.*" s))))

(defn number [x]
  (let [n (js/parseInt x)]
    (when (integer? n) n)))

(defn cell-value [xy state]
  (let [cell (state xy)]
    (if (:formula cell)
      (read-formula (:formula cell) state)
      ((some-fn number identity) (:value cell)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Formula interpreter

(defn- tokenize-formula [s]
  (rest (rest (string/split s #"(=|/|\*|\+|\-)"))))

(defn- parse-formula [s env]
  (for [x (tokenize-formula s)]
    (or ({"/" / "*" * "+" + "-" -} x)
        (number x)
        (cell-value x env))))

(defn- infix [x & xs]
  (reduce (fn [res [op y]] (op res y)) x (partition 2 xs)))

(defn read-formula [s env]
  (apply infix (parse-formula s env)))

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

(defn- display-edited [state]
  [[:display (:edit state)] :value (cell-value (:focus state) state)])

(defn- blur [state]
  (case (:mode state)
    :formula [[] state]
    [[(display-mode (:edit state))
      (display-edited state)]
     (dissoc state :source)]))

(defn- enter [state]
  [[(display-mode (:edit state))
    (display-mode (:source state))
    (display-edited state)]
   (dissoc state :mode :edit :source)])

(defn- click [xy state]
  (case (:mode state)
    :formula
    (let [formula (str (get-in state [(:focus state) :formula]) xy)]
      [[(source-mode xy)
        (display-mode (:source state))
        [[:edit (:focus state)] :value formula]]
       (assoc state :source xy (:focus state) {:formula formula})])
    [[(edit-mode xy)
      (display-mode (:edit state))]
     (assoc state :edit xy)]))

(defn- edit [xy v state]
  (if (formula? v)
    [[(formula-mode xy)] (assoc state :mode :formula xy {:formula v})]
    [[] (assoc state :mode :value xy {:value v})]))

(defn- shortcut [k state]
  (case (:mode state)
    :formula
    (if-let [op ({191 "/" 56 "*" 189 "-" 187 "+"} k)]
      (let [xy (:edit state)
            v (str (:formula (state xy)) op)]
        [[[[:edit xy] :value v]] (assoc state xy {:formula v})])
      (if (= k 13)
        (enter state)
        [[] state]))
    [[] state]))

(defn behavior [message state]
  (println message state)
  (match message
    [[:edit xy] :focus? true] [[] (assoc state :focus xy)]
    [[:edit _] :focus? false] (blur state)
    [[:edit xy] :value v]     (edit xy v state)
    [[:edit _] :key-down 13]  (enter state)
    [[:display xy] :click _]  (click xy state)
    [:shortcut :key k]        (shortcut k state)
    :else [[] state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extern

(defn extern [return _]
  (set! js/document.body.onkeydown
    (fn [e]
      (when (= e.target js/document.body)
        (return [:shortcut :key e.keyCode])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec

(defn- cell [x y]
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

(defn- row [y width]
  {:entity [:row y]
   :render combo/div
   :class "row"
   :units (map #(cell % y) ["A" "B" "C" "D" "E"])})

(defn table []
  {:entity :table
   :render combo/div
   :units (map row (range 10))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public API

(defn spreadsheet [app owner]
  (om/component
    (om/build combo/view nil
      {:opts {:extern extern
              :behavior behavior
              :layout combo/bootstrap-layout
              :units [(table)]}})))
