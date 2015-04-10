(ns many-worlds.console
  (:require [figwheel.client :as fw]
            [om.core :as om]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;;
;; Utilities
;;

(defn frame-url
  [world-base-url]
  (str world-base-url "/frame.png"))

;;
;; State
;;

(defonce !state (atom {}))

;;
;; Components
;;

(defn previews-component
  "The component that generates the grid of image previews of the various worlds
  that the console is configured to connect to."
  [worlds owner]
  (reify
    om/IRender
    (render [_]
      (html
        [:div.worlds
         (for [world worlds]
           [:div.world {:key world}
            [:img {:src (frame-url world)}]])]))))

(defn add-world
  [state owner]
  (let [new-world (.-value (om/get-node owner "new-world"))]
    (when-not (empty? new-world)
      (om/transact! state :worlds #((fnil conj []) % new-world)))))

(defn add-world-component
  "The component that allows the user to add a 'world' (a URL for an instance of the many-worlds API)."
  [state owner]
  (reify
    om/IRender
    (render [_]
      (html
        [:div#add-world
         [:span "Add world server:"]
         [:input {:type :text :ref "new-world"}]
         [:button {:on-click (fn [_] (add-world state owner))} "Add World"]]))))

;;
;; Om App
;;

(om/root
  (fn [state owner]
    (reify
      om/IRender
      (render [_]
        (html
          [:div#many-worlds-console
           (om/build previews-component (:worlds state))
           (om/build add-world-component state)]))))

  !state
  {:target (. js/document (getElementById "console"))})

;;
;; Figwheel Client Initialization
;;

(fw/start
  {:websocket-url "ws://localhost:3449/figwheel-ws"})

(swap! !state assoc :text "Hello, world!")
