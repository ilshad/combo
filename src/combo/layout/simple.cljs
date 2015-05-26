(ns combo.layout.simple
  (:require [om.dom :as dom :include-macros true]))

(defn simple-layout [unit opts]
  (apply dom/div nil (map unit (:units opts))))
