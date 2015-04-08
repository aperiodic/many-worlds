(ns many-worlds.console
  (:require [figwheel.client :as fw]
            [om.core :as om]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defonce !state (atom {}))

(om/root
  (fn [state owner]
    (reify
      om/IWillMount
      (will-mount [_] (println "mounting component..."))

      om/IRender
      (render [_]
        (html [:h1 (:text state)]))

      om/IWillUnmount
      (will-unmount [_] (println "unmounting component...."))))

  !state
  {:target (. js/document (getElementById "console"))})

(fw/start
  {:websocket-url "ws://localhost:3449/figwheel-ws"
   :on-jsload (fn [] (println "reloaded"))})

(swap! !state assoc :text "Hello, world!")
