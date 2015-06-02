(ns examples.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [examples.spreadsheet :as spreadsheet]))

(enable-console-print!)

(defn about [app owner]
  (om/component
    (dom/div
      (dom/p
        (dom/h4 "Links:")
        (dom/ul
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo" :target "_blank"}
              "Combo home page"))
          (dom/li
            (dom/a {:href "https://github.com/ilshad/combo/blob/master/examples/examples/spreadsheet.cljs" :target "_blank"}
              "Spreadsheet source code")))))))

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
        (menu-item app :spreadsheet "Spreadsheet")))))

(def screens
  {:about about
   :spreadsheet spreadsheet/spreadsheet})

(defn root [app owner]
  (om/component
    (dom/div {:class "container"}
      (navbar app)
      (om/build (screens (:screen app)) app))))

(defn main []
  (om/root root (atom {:screen :spreadsheet})
    {:target js/document.body}))

(set! (.-onload js/window) main)
