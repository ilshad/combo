(ns combo.lib.layout.dumb
  (:require [om.dom :as dom :include-macros true]))

(defn dumb-layout [widget opts]
  (apply dom/div nil (map widget (:widgets opts))))
