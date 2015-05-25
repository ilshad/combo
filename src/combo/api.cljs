(ns combo.api
  (:require [combo.core              :as core]
            [combo.layouts.bootstrap :as bootstrap]
            [combo.widgets.render    :as render]))

(def view                    core/view)

(def bootstrap-layout        bootstrap/bootstrap-layout)

(def input                   render/input)
(def textarea                render/textarea)
(def select                  render/select)
(def checkbox                render/checkbox)
(def button                  render/button)
(def div                     render/div)
(def span                    render/span)
(def a                       render/a)
