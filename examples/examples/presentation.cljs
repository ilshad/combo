(ns examples.presentation
  (:require [combo.api :as combo]
            [cljs.core.async :as async]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils

(defn- remove-from-vector [v pos]
  (vec (concat (subvec v 0 pos) (subvec v (inc pos) (count v)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Behavior

(defn- new-page []
  {:content (str (rand-int 256))})

(defn- page->thumb [page id]
  {:id id :content (apply str (take 100 (:content page)))})

(defn- thumbs-message [state]
  [:thumbs :items (map page->thumb (:pages state) (range))])

(defn- select-page-messages [id state]
  [[:canvas :value (if (nil? id) "" (:content ((:pages state) id)))]
   [:thumbs :active id]])

(defn- init []
  (let [state {:pages [(new-page)] :active 0}]
    [(cons (thumbs-message state) (select-page-messages 0 state))
     state]))

(defn- select-page [id state]
  (let [state (assoc state :active id)]
    [(select-page-messages id state) state]))

(defn- add-page [state]
  (let [state (update-in state [:pages] #(conj % (new-page)))
        id (dec (count (:pages state)))]
    [(cons (thumbs-message state) (select-page-messages id state))
     (assoc state :active id)]))

(defn- del-page [state]
  (if (> (count (:pages state)) 1)
    (let [id (:active state)
          state (update-in state [:pages] #(remove-from-vector % id))
          new-id (dec (count (:pages state)))]
      [(cons (thumbs-message state) (select-page-messages new-id state))
       (assoc state :active new-id)])
    [[] state]))

(defn- canvas-content []
  (.-innerHTML (.getElementById js/document "canvas")))

(defn- save-page-state [state]
  (update-in state [:pages]
    (fn [pages]
      (assoc pages (:active state) {:content (canvas-content)}))))

(defn- save-page [state]
  (let [state (save-page-state state)]
    [[(thumbs-message state)] state]))

(defn- text [k state]
  (.execCommand js/document (name k))
  [[] state])

(defn behavior [message state]
  (match message
    [:combo/init   _ _] (init)
    [:thumbs :click id] (select-page id state)
    [[:page :add]  _ _] (add-page state)
    [[:page :del]  _ _] (del-page state)
    [[:page :save] _ _] (save-page state)
    [[:text k]     _ _] (text k state)
    :else [[] state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Render

(defn render-thumbs [owner _]
  (dom/div {:class "thumbs col-xs-2"}
    (dom/div {:class "thumbs-inner"}
      (for [i (om/get-state owner :items)]
        (dom/div {:class (str "thumb"
                           (when (= (:id i) (om/get-state owner :active))
                             " active"))
                  :on-click (fn [e]
                              (async/put! (om/get-state owner :return-chan)
                                [:thumbs :click (:id i)])
                              (.preventDefault e))
                  :dangerouslySetInnerHTML #js {:__html (:content i)}})))))

(defn render-canvas [owner _]
  (dom/div {:class "workspace col-xs-10"}
    (dom/div {:id "canvas"
              :class "canvas"
              :contentEditable ""
              :dangerouslySetInnerHTML
              #js {:__html (om/get-state owner :value)}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec

(def page-actions
  [[[:page :add]  "plus"]
   [[:page :save] "save"]
   [[:page :del]  "trash"]])

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

(defn button [[entity icon]]
  {:entity entity
   :render combo/button
   :value (dom/i {:class (str "fa fa-" icon)})})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn presentation [app owner]
  (om/component
    (om/build combo/view nil
      {:opts {:behavior behavior
              :layout combo/bootstrap-layout
              :units [{:render combo/div
                       :class "row"
                       :units [{:render combo/div
                                :class "btn-group col-xs-2"
                                :units (map button page-actions)}
                               {:render combo/div
                                :class "btn-group col-xs-4 no-padding"
                                :units (map button workspace-actions)}
                               {:render combo/div
                                :class "btn-group col-xs-1"
                                :units (map button presentation-actions)}]}
                      {:render combo/div
                       :class "row"
                       :units [{:entity :thumbs
                                :render render-thumbs}
                               {:entity :canvas
                                :render render-canvas}]}]}})))
