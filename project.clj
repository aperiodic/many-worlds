(defproject many-worlds "0.1.0-SNAPSHOT"
  :description "Interactive, parallel state space exploration for quil sketches."
  :url "https://github.com/aperiodic/many-worlds"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-beta1"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-ajax "0.3.11"]
                 [compojure "1.3.3"]
                 [figwheel "0.2.5"]
                 [org.omcljs/om "0.8.8"]
                 [quil "1.7.0"]
                 [aperiodic/qutils "0.1.0-SNAPSHOT"]
                 [ring "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [sablono "0.3.4"]]

  :source-paths ["src/clj"]

  :profiles {:dev {:source-paths ["src/clj" "dev"]
                   :plugins [[lein-cljsbuild "1.0.4"]
                             [lein-figwheel "0.2.5" :exclusions [org.clojure/clojure]]
                             [lein-ring "0.9.3"]]}}

  :cljsbuild {
    :builds {:dev {:source-paths ["src/cljs"]
                   :compiler {:output-to "resources/public/js/compiled/main.js"
                              :output-dir "resources/public/js/compiled/out"
                              :optimizations :none
                              :source-map true}}}}

  :figwheel {:server-port 3449}
  :ring {:handler many-worlds.api.dev/dev-handler})
