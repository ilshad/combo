(ns combo-demo.presentation
  (:require [combo.api :as combo]
            [cljs.core.async :as async]
            [cljs.core.match :refer-macros [match]]
            [om-tools.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils

(defn- remove-from-vector [v pos]
  (vec (concat (subvec v 0 pos) (subvec v (inc pos) (count v)))))

(defn- inner-html [content attrs]
  (dom/div
    (merge {:dangerouslySetInnerHTML #js {:__html content}}
      attrs)))

(defn- canvas-content []
  (.-innerHTML (.getElementById js/document "canvas")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Play mode

(defn play-popup! [pages]
  (let [popup (.open js/window "" "" "width=1015,height=720")]
    (om/root
      (fn [_ _]
        (om/component
          (dom/div {:style {:font "45px monospace"}}
            (map #(inner-html (:content %) {:style {:height "720px"}})
              pages))))
      nil {:target (.. popup -document -body)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Behavior

(defn- new-page [id]
  {:content (str (inc id))})

(defn- page->thumb [page id]
  {:id id :content (apply str (take 100 (:content page)))})

(defn- update-message [state id]
  [[:canvas :value (if (nil? id) "" (:content ((:pages state) id)))]
   [:thumbs :items (map page->thumb (:pages state) (range))]
   [:thumbs :active id]])

(defn- save-page-state [state]
  (update-in state [:pages]
    (fn [pages]
      (assoc pages (:active state) {:content (canvas-content)}))))

(defn- init []
  (let [state {:pages [(new-page 0)] :active 0}]
    [state (update-message state 0)]))

(defn- select-page [state id]
  (let [state (assoc (save-page-state state) :active id)]
    [state (update-message state id)]))

(defn- add-page [state]
  (let [id (count (:pages state))
        state (update-in (save-page-state state) [:pages]
                #(conj % (new-page id)))]
    [(assoc state :active id) (update-message state id)]))

(defn- del-page [state]
  (if (> (count (:pages state)) 1)
    (let [id (:active state)
          state (update-in state [:pages] #(remove-from-vector % id))
          new-id (dec (count (:pages state)))]
      [(assoc state :active new-id) (update-message state new-id)])
    [state []]))

(defn- save-page [state]
  (let [state (save-page-state state)]
    [state (update-message state (:active state))]))

(defn- play [state]
  (play-popup! (:pages state))
  [state []])

(defn- text [state k]
  (.execCommand js/document (name k))
  [state []])

(defn behavior [state event]
  (match event
    [:combo/init   _ _] (init)
    [:thumbs :click id] (select-page state id)
    [[:page :add]  _ _] (add-page state)
    [[:page :del]  _ _] (del-page state)
    [[:page :save] _ _] (save-page state)
    [[:file :play] _ _] (play state)
    [[:text k]     _ _] (text state k)
    :else [state []]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Render

(defn render-thumbs [m]
  (dom/div {:class "thumbs col-xs-2"}
    (dom/div {:class "thumbs-inner"}
      (for [i (:items m)]
        (inner-html (:content i)
          {:class (str "thumb"
                    (when (= (:id i) (:active m))
                      " active"))
           :on-click (fn [e]
                       (async/put! (:input-chan m) [:thumbs :click (:id i)])
                       (.preventDefault e))})))))

(defn render-canvas [m]
  (dom/div {:class "workspace col-xs-10"}
    (inner-html (:value m)
      {:id "canvas" :class "canvas" :contentEditable ""})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec

(def page-actions
  [[[:page :add]           "plus"]
   [[:page :save]          "save"]
   [[:page :del]           "trash"]])

(def workspace-actions
  [[[:text :bold]          "bold"]
   [[:text :italic]        "italic"]
   [[:text :justifyLeft]   "align-left"]
   [[:text :justifyCenter] "align-center"]
   [[:text :justifyRight]  "align-right"]
   [[:text :indent]        "indent"]
   [[:text :outdent]       "outdent"]])

(def file-actions
  [[[:file :play]          "play"]])

(defn button [[id icon]]
  {:id id
   :render combo/button
   :value (dom/i {:class (str "fa fa-" icon)})})

(def units
  [{:render combo/div
    :class "row"
    :units [{:render combo/div
             :class "btn-group col-xs-2"
             :units (map button page-actions)}
            {:render combo/div
             :class "btn-group col-xs-4 no-padding"
             :units (map button workspace-actions)}
            {:render combo/div
             :class "btn-group col-xs-1"
             :units (map button file-actions)}]}
   {:render combo/div
    :class "row"
    :units [{:id :thumbs :render render-thumbs}
            {:id :canvas :render render-canvas}]}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public

(defn presentation [_ _]
  (om/component
    (om/build combo/view nil
      {:opts {:behavior behavior
              :layout combo/bootstrap-layout
              :units units}})))
