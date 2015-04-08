(ns many-worlds.console
  (:require [figwheel.client :as fw]
            [om.core :as om]
            [om.dom :as dom]))

(enable-console-print!)

(defonce !state (atom {}))

(om/root
  (fn [state owner]
    (reify
      om/IWillMount
      (will-mount [_] (println "mounting component..."))

      om/IRender
      (render [_]
        (dom/h1 nil (:text state)))

      om/IWillUnmount
      (will-unmount [_] (println "unmounting component...."))))

  !state
  {:target (. js/document (getElementById "console"))})

(fw/start
  {:websocket-url "ws://localhost:3449/figwheel-ws"
   :on-jsload (fn [] (println "reloaded"))})

(swap! !state assoc :text "Hello, world!")
