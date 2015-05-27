(ns combo.layout.simple
  (:require [om.dom :as dom :include-macros true]))

(defn simple-layout [build {units :units}]
  (apply dom/div nil (map build units)))
