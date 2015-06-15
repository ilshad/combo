(ns examples.presentation
  (:require [combo.api :as combo]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commit

(defn commit [{:keys [message data] :as args}]
  (println "commit:" args)
  (match message
    [_ :add [id content]]
    (om/transact! data [:slides] #(assoc % id {:content content}))
    [_ :del id]
    (om/transact! data [:slides] #(dissoc % id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Behavior

(defn- text [k state]
  (.execCommand js/document (name k))
  [[] state])

(defn- add-slide [state]
  (let [id (inc (:active state 1))
        content (str "Slide #" (rand-int 256))]
    [[[:combo/commit :add [id content]]]
     (assoc state id {:content content} :active id)]))

(defn- del-slide [state]
  [[[:combo/commit :del (:active state)]] state])

(defn- show-slide [id state]
  (let [content (-> state :active state :content)]
    (println "content is:" content)
    [[[[:thumb id] :class "thumb active"]
      [[:thumb (:active state)] :class "thumb"]
      [:canvas :value content]]
     (assoc state :active id)]))

(defn behavior [message state]
  (println message state)
  (match message
    [[:text k]     _ _] (text k state)
    [[:slide :add] _ _] (add-slide state)
    [[:slide :del] _ _] (del-slide state)
    [[:thumb id]   _ _] (show-slide id state)
    :else [[] state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec

(defn button [[entity icon]]
  {:entity entity
   :render combo/button
   :value (dom/i {:class (str "fa fa-" icon)})})

(def slide-actions
  [[[:slide :add] "plus"]
   [[:slide :del] "trash"]])

(def workspace-actions
  [[[:text :bold]          "bold"]
   [[:text :italic]        "italic"]
   [[:text :justifyLeft]   "align-left"]
   [[:text :justifyCenter] "align-center"]
   [[:text :justifyRight]  "align-right"]
   [[:text :indent]        "indent"]
   [[:text :outdent]       "outdent"]])

(def presentation-actions
  [[[:presentation :play] "play"]])

(def toolbar
  {:render combo/div
   :class "row"
   :units [{:render combo/div
            :class "btn-group col-xs-2"
            :units (map button slide-actions)}
           {:render combo/div
            :class "btn-group col-xs-4 no-padding"
            :units (map button workspace-actions)}
           {:render combo/div
            :class "btn-group col-xs-1"
            :units (map button presentation-actions)}]})

(defn thumb [[id slide]]
  {:render combo/div
   :entity [:thumb id]
   :class "thumb"
   :value id})

(defn thumbs [slides]
  {:render combo/div
   :class "thumbs col-xs-2"
   :units [{:render combo/div
            :class "thumbs-inner"
            :units (map thumb slides)}]})

(def workspace
  {:render combo/div
   :class "workspace presentation-workspace col-xs-10"
   :units [{:entity :canvas
            :render combo/div
            :class "canvas"
            :attrs {:contentEditable ""}}]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn presentation [app owner]
  (om/component
    (om/build combo/view app
      {:opts {:commit commit
              :behavior behavior
              :layout combo/bootstrap-layout
              :units [toolbar
                      {:render combo/div
                       :class "row"
                       :units [(thumbs (:slides app))
                               workspace]}]}})))
