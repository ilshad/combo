(def +version+ "0.3.1")

(task-options!
  pom {:project     'combo
       :version     +version+
       :description "Message driven component for Om"
       :url         "https://github.com/ilshad/combo"
       :scm         {:url "https://github.com/ilshad/combo"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(set-env!
  :source-paths #{"src"}
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure       "1.7.0" :scope "provided"]
                  [org.clojure/clojurescript "0.0-3308"    :scope "provided"]
                  [org.clojure/core.async    "0.1.346.0-17112a-alpha"
                                                     :scope "provided"]
                  [org.omcljs/om             "0.9.0" :scope "provided"]

                  [adzerk/boot-cljs          "0.0-3308-0"     :scope "test"]
                  [adzerk/boot-reload        "0.3.1"          :scope "test"]
                  [adzerk/boot-cljs-repl     "0.1.9"          :scope "test"]
                  [pandeiro/boot-http        "0.6.3-SNAPSHOT" :scope "test"]
                  [adzerk/bootlaces          "0.1.11"         :scope "test"]])

(require '[adzerk.boot-cljs      :refer [cljs]]
         '[adzerk.boot-reload    :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[pandeiro.boot-http    :refer [serve]]
         '[adzerk.bootlaces      :refer :all])

(bootlaces! +version+)

;; Development

(defn dev-env! []
  (merge-env!
    :source-paths #{"dev"}
    :resource-paths #{"dev"}
    :dependencies '[[org.clojure/core.match "0.3.0-alpha4"]
                    [prismatic/om-tools "0.3.11"]]))

(deftask dev []
  (dev-env!)
  (comp (serve :dir "target")
        (watch)
        (reload :on-jsload 'dev.core/main)
        (cljs-repl)
        (cljs :optimizations :none
              :source-map    true
              :unified-mode  true)))

(deftask dev-advanced []
  (dev-env!)
  (comp (serve :dir "target")
        (cljs :optimizations :advanced)
        (wait)))

;; Demo

(defn demo-env! []
  (merge-env!
    :source-paths #{"demo"}
    :resource-paths #{"demo"}
    :dependencies '[[org.clojure/core.match "0.3.0-alpha4"]
                    [prismatic/om-tools "0.3.10"]]))

(deftask demo []
  (demo-env!)
  (comp (serve :dir ".")
        (watch)
        (reload :on-jsload 'combo-demo.core/main)
        (cljs-repl)
        (cljs :optimizations :none
              :source-map    true
              :unified-mode  true)))

;; Release

(deftask clojars-snapshot []
  (comp (build-jar) (push-snapshot)))

(deftask clojars-release []
  (comp (build-jar) (push-release)))

(deftask gh-pages []
  (demo-env!)
  (cljs :optimizations :advanced))
