(task-options!
  pom {:project 'combo
       :version "0.1.0"})

(set-env!
  :source-paths #{"src" "examples"}
  :resource-paths #{"examples"}
  :dependencies '[[org.clojure/clojure "1.7.0-beta1"]
                  [org.clojure/clojurescript "0.0-3196"]
                  [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                  [adzerk/boot-cljs "0.0-2814-4"]
                  [adzerk/boot-reload "0.2.6"]
                  [adzerk/boot-cljs-repl "0.1.9"]
                  [pandeiro/boot-http "0.6.3-SNAPSHOT"]
                  [org.omcljs/om "0.8.8"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[pandeiro.boot-http :refer [serve]])

(deftask dev []
  (comp (serve :dir "target")
        (watch)
        ;(speak)
        (reload :on-jsload 'examples/main)
        (cljs-repl)
        (cljs :optimizations :none
              :source-map true
              :unified-mode true
              :compiler-options
              {:warnings {:single-segment-namespace false}})))
