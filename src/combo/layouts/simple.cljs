(ns combo.layouts.simple
  (:require [om.dom :as dom :include-macros true]))

(defn simple-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))
