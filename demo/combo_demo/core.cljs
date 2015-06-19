(ns combo-demo.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
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
        "Combo Demo")
      (dom/ul {:class "nav navbar-nav nav-pills"}
        (menu-item app :about        "About")
        (menu-item app :spreadsheet  "Spreadsheet")
        (menu-item app :presentation "Presentation")
        (menu-item app :login        "Login")))))

(def screens
  {:about        about
   :spreadsheet  spreadsheet/spreadsheet
   :presentation presentation/presentation
   :login        login/login})

(defn root [app owner]
  (om/component
    (dom/div {:class "container"}
      (navbar app)
      (om/build (screens (:screen app)) app))))

(defn main []
  (om/root root (atom {:screen :about})
    {:target js/document.body}))

(set! (.-onload js/window) main)
