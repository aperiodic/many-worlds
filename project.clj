(defproject many-worlds "0.1.0-SNAPSHOT"
  :description "Interactive, parallel state space exploration for quil sketches."
  :url "https://github.com/aperiodic/many-worlds"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [compojure "1.3.2"]
                 [figwheel "0.2.5"]
                 [org.omcljs/om "0.8.8"]
                 [ring "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [org.clojars.aperiodic/quil "1.6.1"]
                 [aperiodic/qutils "0.1.0-SNAPSHOT"]]

  :source-paths ["src/clj"]

  :profiles {:dev {:source-paths ["src/clj" "dev"]
                   :plugins [[lein-cljsbuild "1.0.4"]
                             [lein-figwheel "0.2.5"]
                             [lein-ring "0.9.3"]]}}

  :cljsbuild {
    :builds {:dev {:source-paths ["src/cljs"]
                   :compiler {:output-to "resources/public/js/compiled/main.js"
                              :output-dir "resources/public/js/compiled/out"
                              :optimizations :none
                              :source-map true}}}}

  :figwheel {:server-port 3449}
  :ring {:handler many-worlds.api.dev/dev-handler})
