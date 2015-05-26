(ns combo.layout.simple
  (:require [om.dom :as dom :include-macros true]))

(defn simple-layout [build opts]
  (apply dom/div nil (map build (:units opts))))
