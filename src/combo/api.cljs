(ns combo.api
  (:require [combo.core   :as core]
            [combo.layout :as layout]
            [combo.widget :as widget]))

(def view                    core/view)

(def dumb-layout             layout/dumb-layout)
(def bootstrap-form-layout   layout/bootstrap-form-layout)

(def input                   widget/input)
(def textarea                widget/textarea)
(def select                  widget/select)
(def checkbox                widget/checkbox)
(def button                  widget/button)
(def div                     widget/div)