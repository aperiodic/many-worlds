(ns many-worlds.console
  (:require [figwheel.client :as fw]))

(enable-console-print!)

(println "Hello from many-worlds.console!")

(fw/start
  {:websocket-url "ws://localhost:3449/figwheel-ws"
   :on-jsload (fn [] (println "reloaded"))})
