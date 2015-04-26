(ns combo.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [combo.layout :as layout]))

(defn widget [data owner opts]
  (om/component ((:render opts) data owner opts)))

(defn view [data owner opts]
  (om/component
    (let [layout (:layout opts layout/dumb)]
      (layout
        (fn [{:keys [name]}]
          (om/build widget (data name)))
        opts))))
