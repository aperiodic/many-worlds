(ns many-worlds.console
  (:require [figwheel.client :as fw]
            [om.core :as om]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;;
;; Utilities
;;

(defn frame-url
  [world-url t]
  (str world-url "/frame.png?t=" t))

;;
;; State
;;

(defonce !state (atom {:worlds []}))

;;
;; Components
;;

(defn previews-component
  "The component that generates the grid of image previews of the various worlds
  that the console is configured to connect to."
  [state owner]
  (reify
    om/IRender
    (render [_]
      (let [t (:t state 0)]
        (html
          [:div.worlds
           (for [world (:worlds state)]
             [:div.world {:key world}
              [:img {:src (frame-url world t)}]])])))))

(defn add-world
  [state owner]
  (let [new-world (.-value (om/get-node owner "new-world"))]
    (when-not (empty? new-world)
      (om/transact! state :worlds #((fnil conj []) % new-world))
      (om/set-state! owner :text ""))))

(defn- update-text
  [event owner {text :text}]
  (om/set-state! owner :text (.. event -target -value)))

(defn add-world-component
  "The component that allows the user to add a 'world' (a URL for an instance of
  the many-worlds API)."
  [state owner]
  (reify
    om/IInitState
    (init-state [_] {:text ""})
    om/IRenderState
    (render-state [_ {text :text :as comp-state}]
      (html
        [:div#add-world
         [:span "Add world server:"]
         [:input {:type "text", :ref "new-world", :value text
                  :on-change #(update-text % owner comp-state)}]
         [:button {:on-click #(add-world state owner)}
          "Add World"]]))))

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
           (om/build previews-component state)
           (om/build add-world-component state)]))))

  !state
  {:target (. js/document (getElementById "console"))})

;;
;; Figwheel Client Initialization
;;

(fw/start
  {:websocket-url "ws://localhost:3449/figwheel-ws"})
