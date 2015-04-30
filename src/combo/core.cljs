(ns combo.core
  "Public API"
  (:require [combo.view   :as view]
            [combo.layout :as layout]
            [combo.widget :as widget]))

(def view                    view/view)

(def dumb-layout             layout/dumb-layout)
(def bootstrap-form-layout   layout/bootstrap-form-layout)

(def input                   widget/input)
(def textarea                widget/textarea)
(def select                  widget/select)
(def checkbox                widget/checkbox)
(def button                  widget/button)
