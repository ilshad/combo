(task-options!
  pom {:project     'ilshad/combo
       :version     "0.1.0-SNAPSHOT"
       :description ""
       :url         "https://github.com/ilshad/combo"
       :scm         {:url "https://github.com/ilshad/combo"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(set-env!
  :source-paths #{"src"}
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure       "1.7.0-beta1"]
                  [org.clojure/clojurescript "0.0-3196"]
                  [org.clojure/core.async    "0.1.346.0-17112a-alpha"]
                  [org.omcljs/om             "0.8.8"]
                  
                  [adzerk/boot-cljs          "0.0-2814-4"]
                  [adzerk/boot-reload        "0.2.6"]
                  [adzerk/boot-cljs-repl     "0.1.9"]
                  [pandeiro/boot-http        "0.6.3-SNAPSHOT"]])

(require '[adzerk.boot-cljs      :refer [cljs]]
         '[adzerk.boot-reload    :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[pandeiro.boot-http    :refer [serve]])

(defn dev-env! []
  (merge-env!
    :source-paths #{"dev"}
    :resource-paths #{"dev"}
    :dependencies '[[org.clojure/core.match "0.3.0-alpha4"]]))

(deftask dev []
  (dev-env!)
  (comp (serve :dir "target")
        (watch)
        (reload :on-jsload 'combo.dev/main)
        (cljs-repl)
        (cljs :optimizations :none
              :source-map    true
              :unified-mode  true)))

(deftask staging []
  (dev-env!)
  (comp (serve :dir "target")
        (cljs :optimizations :advanced)
        (wait)))
