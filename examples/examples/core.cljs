(ns examples.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [examples.presentation :as presentation]
            [examples.spreadsheet :as spreadsheet]
            [examples.editor :as editor]))

(enable-console-print!)

(defn- combo-link [title]
  (dom/a {:href "https://github.com/ilshad/combo" :target "_blank"}
    title))

(defn about [app owner]
  (om/component
    (dom/div
      (dom/p {:class "jumbotron"}
        "Here are examples of components built with " (combo-link "Combo")
        ". Please note that they are tested in Google Chrome.")
      (dom/p
        (dom/h4 "Links:")
        (dom/ul
          (dom/li (combo-link "Combo home page"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/examples/examples/spreadsheet.cljs" :target "_blank"}
              "Spreadsheet source code"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/examples/examples/editor.cljs" :target "_blank"}
              "Editor source code"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/examples/examples/presentation.cljs" :target "_blank"}
              "Presentation source code")))))))

(defn menu-item [app screen title]
  (dom/li {:class (when (= (:screen app) screen) "active")}
    (dom/a {:href "#"
            :on-click (fn [e]
                        (om/update! app :screen screen)
                        (.preventDefault e))}
      title)))

(defn navbar [app]
  (dom/div {:class "navbar"}
    (dom/div {:class "navbar-header"}
      (dom/span {:class "navbar-brand"}
        "Combo Examples")
      (dom/ul {:class "nav navbar-nav nav-pills"}
        (menu-item app :about "About")
        (menu-item app :spreadsheet "Spreadsheet")
        (menu-item app :editor "Editor")
        (menu-item app :presentation "Presentation")))))

(def screens
  {:about about
   :spreadsheet spreadsheet/spreadsheet
   :editor editor/editor
   :presentation presentation/presentation})

(defn root [app owner]
  (om/component
    (dom/div {:class "container"}
      (navbar app)
      (om/build (screens (:screen app)) app))))

(def init-data
  {:screen :about
   :slides (sorted-map 1 {:title "Foo"})})

(defn main []
  (om/root root (atom init-data)
    {:target js/document.body}))

(set! (.-onload js/window) main)
