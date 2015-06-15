(ns examples.editor
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(defn behavior [message state]
  (match message
    [[:text k] _ _] (.execCommand js/document (name k))
    :else nil)
  [[] state])

(defn button [[entity icon]]
  {:entity entity
   :render combo/button
   :value (dom/i {:class (str "fa fa-" icon)})})

(def actions
  [[[:text :bold]          "bold"]
   [[:text :italic]        "italic"]
   [[:text :justifyLeft]   "align-left"]
   [[:text :justifyCenter] "align-center"]
   [[:text :justifyRight]  "align-right"]
   [[:text :indent]        "indent"]
   [[:text :outdent]       "outdent"]])

(defn editor [app owner]
  (om/component
    (om/build combo/view nil
      {:opts {:behavior behavior
              :layout combo/bootstrap-layout
              :units [{:render combo/div
                       :class "btn-group"
                       :units (map button actions)}
                      {:render combo/div
                       :class "workspace"
                       :units [{:entity :canvas
                                :render combo/div
                                :class "canvas"
                                :attrs {:contentEditable ""}}]}]}})))
