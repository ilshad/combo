(ns combo.api
  (:require [combo.core              :as core]
            [combo.layouts.bootstrap :as bootstrap]
            [combo.widgets.render    :as render]))

(def view                    core/view)

(def bootstrap-layout        bootstrap/bootstrap-layout)

(def input                   render/input)
(def select                  render/select)
(def textarea                render/textarea)
(def checkbox                render/checkbox)
(def button                  render/button)
(def span                    render/span)
(def div                     render/div)
(def a                       render/a)
