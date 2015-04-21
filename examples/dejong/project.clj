(defproject many-worlds/dejong "0.1.0-SNAPSHOT"
  :description "An example of using the many worlds quil library."
  :url "https://github.com/aperiodic/many-worlds"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-beta1"]
                 [many-worlds "0.1.0-SNAPSHOT"]
                 [quil "1.7.0"]]
  :main ^:skip-aot many-worlds.example.dejong
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
