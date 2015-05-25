(ns combo.api
  (:require [combo.core                 :as core]
            [combo.lib.layout.dumb      :as dumb]
            [combo.lib.layout.bootstrap :as bootstrap]
            [combo.lib.widget.render    :as widget]))

(def view                    core/view)

(def dumb-layout             dumb/dumb-layout)
(def bootstrap-layout        bootstrap/bootstrap-layout)

(def input                   widget/input)
(def textarea                widget/textarea)
(def select                  widget/select)
(def checkbox                widget/checkbox)
(def button                  widget/button)
(def div                     widget/div)
(def a                       widget/a)
