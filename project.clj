(defproject many-worlds "0.1.0-SNAPSHOT"
  :description "Interactive, parallel state space exploration for quil sketches."
  :url "https://github.com/aperiodic/many-worlds"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.2"]
                 [ring "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [org.clojars.aperiodic/quil "1.6.1"]
                 [aperiodic/qutils "0.1.0-SNAPSHOT"]]
  :source-paths ["src/clj"])
