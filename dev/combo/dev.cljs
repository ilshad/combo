(ns combo.dev
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.match :refer-macros [match]]
            [combo.api :as combo]))

(enable-console-print!)

(defn- validate-user [s]
  (when (< (count s) 20)
    s))

(defn- display-result [state]
  (apply str
    (interpose ", "
      (vals (select-keys state [:user :city :note])))))

(defn behavior [message state]
  (match message

    [:enable _ value]
    [[[:note :disabled (not value)]] state]

    [entity :value value]
    (let [new-state (assoc state entity value)
          messages [[:result :value (display-result new-state)]
                    [:user   :group-class (case entity
                                            :city "has-error"
                                            :note "has-warning"
                                            :user "has-success")]]]
      [messages new-state])

    [:save _ _]
    [[[:combo/commit :note (display-result state)]] state]
    
    :else [[] state]))

(defn view [data chan]
  (om/build combo/view data
    {:init-state {:commit-chan chan}
     :opts {:behavior behavior
            :layout combo/bootstrap-layout
            :widgets [{:entity :user
                       :render combo/input
                       :type "text"
                       :interceptor validate-user}
                      {:entity :reset
                       :render combo/a
                       :href ""
                       :value "OK"}
                      {:entity :city
                       :render combo/select}
                      {:entity :enable
                       :render combo/checkbox
                       :label "Enable note field"}
                      {:entity :note
                       :disabled true
                       :render combo/textarea
                       :label "Note"}
                      {:entity :save
                       :render combo/button
                       :value "Save"
                       :class "btn btn-success btn-block"}
                      {:entity :result
                       :render combo/div}]}}))

(defn root [data owner]
  (reify

    om/IInitState
    (init-state [_]
      {:chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [c (om/get-state owner :chan)]
        (go-loop []
          (let [v (async/<! c)]
            (println "Received message from Combo:" v))
          (recur))))
    
    om/IRender
    (render [_]
      (dom/div #js {:className "container"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className "col-xs-6 col-xs-push-3"}
            (dom/br nil)
            (view data (om/get-state owner :chan))))))))

(def app-state
  (atom {:agree? true
         :note "Hi there"
         :city {:options {"" ""
                          "New York" "New York"
                          "London" "London"
                          "Tolyo" "Tokyo"}}}))

(defn main []
  (om/root root app-state {:target js/document.body}))

(set! (.-onload js/window) main)
