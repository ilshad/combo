(ns examples.spreadsheet
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [clojure.string :as string]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Formula interpreter

(def ops {"/" / "*" * "+" + "-" -})

(defn resolve-var [xy env]
  (get-in env [:cells xy :value]))

(defn string->token [s env]
  (or (ops s)
      (let [n (js/parseInt s)]
        (if (integer? n) n (resolve-var s env)))))

(defn parse-formula [s env]
  (let [[_ a b c] (string/split s #" ")]
    (map #(string->token % env) (list b a c))))

(defn eval-formula [tokens]
  (apply (first tokens) (rest tokens)))

(defn read-formula [s env]
  (eval-formula (parse-formula s env)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Look and feel

(defn cell-mode [mode]
  (fn [xy] [[:cell xy] :class (str mode " cell col-xs-2")]))

(def edit-mode    (cell-mode "edit-mode"))
(def display-mode (cell-mode "display-mode"))
(def formula-mode (cell-mode "formula-mode"))
(def source-mode  (cell-mode "source-mode"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

(defn- formula? [s]
  (string? (first (re-matches #"^=.*" s))))

(defn- write-formula [s state]
  (let [formula (get-in state [:cells (:focus state) :formula])]
    (str formula " " s)))

(defn- cell-value [xy state]
  (let [cell (get-in state [:cells xy])]
    (if (:formula cell)
      (read-formula (:formula cell) state)
      (:value cell))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Brain

(defn behavior [message state]
  (match message

    [[:display xy] :click _]
    (if (= (:mode state) :formula)
      
      (let [formula (write-formula xy state)]
        [[(source-mode xy)
          (display-mode (:source state))
          [[:edit (:focus state)] :value formula]]
         (assoc state
           (:focus state)
           formula :source xy)])
      
      [[(edit-mode xy)
        (display-mode (:edit state))]
       (assoc state :edit xy)])
    
    [[:edit xy] :focus? true]
    [[] (assoc state :focus xy)]
    
    [[:edit xy] :focus? false]
    (if (= (:mode state) :formula)
      [[] state]
      [[(display-mode xy)
        [[:display xy] :value (cell-value (:focus state) state)]]
       (assoc state :mode :done)])

    [[:edit xy] :value v]
    (if (formula? v)
      [[(formula-mode xy)]
       (-> (assoc state :mode :formula)
           (assoc-in [:cells xy :formula] v))]
      [[] (-> (assoc state :mode :value)
              (assoc-in [:cells xy :value] v))])

    [[:edit xy] :key-down 13]
    [[(display-mode xy)
      (display-mode (:source state))
      [[:display xy] :value (get-in state [:cells (:focus state) :value])]]
     (assoc state :mode :done)]
    
    :else [[] state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Spec

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
      {:opts {:behavior behavior
              :layout combo/bootstrap-layout
              :units [(table)]}})))
