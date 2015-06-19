(ns combo-demo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [combo-demo.presentation :as presentation]
            [combo-demo.spreadsheet :as spreadsheet]
            [combo-demo.login :as login]))

(enable-console-print!)

(defn- combo-link [title]
  (dom/a {:href "https://github.com/ilshad/combo" :target "_blank"}
    title))

(defn about [app owner]
  (om/component
    (dom/div
      (dom/p {:class "jumbotron"}
        "This demo app contains example components built with "
        (combo-link "Combo") ". They work properly in Chrome browser.")
      (dom/p
        (dom/ul
          (dom/li (combo-link "Combo home page"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/demo/combo_demo/spreadsheet.cljs" :target "_blank"}
              "Spreadsheet sources"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/demo/combo_demo/presentation.cljs" :target "_blank"}
              "Presentation sources"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/demo/combo_demo/login.cljs" :target "_blank"}
              "Login form sources")))))))

(defn menu-item [app owner screen title]
  (dom/li {:class (when (= (:screen app) screen) "active")}
    (dom/a {:href "#"
            :on-click (fn [e]
                        (async/put! (om/get-state owner :screen) screen)
                        (.preventDefault e))}
      title)))

(defn navbar [app owner]
  (dom/div {:class "navbar"}
    (dom/div {:class "navbar-header"}
      (dom/span {:class "navbar-brand"} "Combo Demo")
      (dom/ul {:class "nav navbar-nav nav-pills"}
        (menu-item app owner :about        "About")
        (menu-item app owner :spreadsheet  "Spreadsheet")
        (menu-item app owner :presentation "Presentation")
        (menu-item app owner :login        "Login")))))

(def screens
  {:about        about
   :spreadsheet  spreadsheet/spreadsheet
   :presentation presentation/presentation
   :login        login/login})

(defcomponent root [app owner]
  (init-state [_]
    {:spinner? false
     :screen (async/chan)})
  (will-mount [_]
    ;; Why use spinner here?
    ;; Spreadsheet creates 161 units, each cell is 3 units.
    ;; Each unit is one Om component, one go routine two channels.
    ;; This is why opening spreadsheet spends about 220-300 ms.
    ;; This problem is easy to solve by writing custom render function
    ;; for cell (see git branch spreadsheet-cell-render-fn) which reduces
    ;; this time to about 100-130 ms. But that code is not so demonstrative.
    ;; Current implementation of Spreadsheet shows flexibility of standard
    ;; render functions.
    (let [c (om/get-state owner :screen)]
      (go-loop []
        (let [screen (async/<! c)]
          (when (= screen :spreadsheet)
            (om/set-state! owner :spinner? true)
            (async/<! (async/timeout 50)))
          (om/update! app :screen screen)
          (async/<! (async/timeout 150))
          (om/set-state! owner :spinner? false))
        (recur))))
  (render-state [_ state]
    (dom/div {:class "container"}
      (when (:spinner? state)
        (dom/div {:class "spinner"}
          (dom/i {:class "fa fa-fw fa-spin fa-spinner"})))
      (navbar app owner)
      (om/build (screens (:screen app)) app))))

(defn main []
  (om/root root (atom {:screen :about})
    {:target js/document.body}))

(set! (.-onload js/window) main)
