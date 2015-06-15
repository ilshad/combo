(ns examples.editor
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(defn- exec [k state]
  (.execCommand js/document (name k) false "")
  [[[:feedback :value (str "Style: " (name k))]] state])

(defn- clean-canvas []
  (apply str (repeat (rand-int 99) " ")))

(defn- new-document [state]
  [[[:canvas   :value (clean-canvas)]
    [:feedback :value "New document..."]]
   state])

(defn behavior [message state]
  (match message
    [[:text k]       _ _] (exec k state)
    [[:file :new]    _ _] (new-document state)
    [[:file :save]   _ _] [[[:feedback :value "Saved!"]] state]
    [:canvas :key-down _] [[[:feedback :value ""]]       state]
    :else [[] state]))

(defn button [[entity icon]]
  {:entity entity
   :render combo/button
   :value (dom/i {:class (str "fa fa-" icon)})})

(def actions
  [[[:file :new]           "file"]
   [[:file :save]          "save"]
   [[:text :bold]          "bold"]
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
                       :class "btn-group pull-left"
                       :units (map button actions)}
                      {:entity :feedback
                       :render combo/div
                       :class "feedback pull-left"}
                      {:render combo/div
                       :class "clear"}
                      {:render combo/div
                       :class "workspace"
                       :units [{:entity :canvas
                                :render combo/div
                                :id "canvas"
                                :class "canvas"
                                :return-key-down? true
                                :attrs {:contentEditable ""}}]}]}})))
