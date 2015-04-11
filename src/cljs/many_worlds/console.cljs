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

(defonce !state
  (atom {:worlds []
         :time {:t 0, :speed 2}}))

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
      (let [t (get-in state [:time :t] 0)]
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
         [:span.label "Add world server:"]
         [:input {:type "text", :ref "new-world", :value text
                  :on-change #(update-text % owner comp-state)}]
         [:button {:on-click #(add-world state owner)}
          "Add World"]]))))

(def delta-t
  "In order to calculate how far to jump around when skipping, multiply this
  delta by the current speed. With the default speed of 1 the jump will be just
  this value, in seconds."
  0.25)

(defn time-component
  [{:keys [t speed] :as time} owner]
  (reify
    om/IRender
    (render [_]
      (let [reset-speed #(om/transact! time :speed (constantly 2))
            ff #(om/transact! time :speed (fn [speed] (+ speed 0.5)))
            fb #(om/transact! time :speed (fn [speed] (max (- speed 0.5) 1.0)))
            skip-back #(om/transact! time
                                     (fn [{:keys [speed] :as time}]
                                       (-> time
                                         (update-in [:t] - (* speed jump-delta-t))
                                         (update-in [:t] max 0))))
            skip-ahead #(om/transact! time
                                      (fn [{:keys [speed] :as time}]
                                        (update-in time [:t] + (* speed delta-t))))]
        (html
          [:div#time
           [:button.skip-back {:on-click skip-back} "⏮"]
           [:button.fast-backward {:on-click fb} "⏪"]
           [:span.time-display (str "t: " (.toFixed t 1))]
           [:span.speed-display (str "(" (.toFixed speed 1) "x)")]
           [:button.fast-forward {:on-click ff} "⏩"]
           [:button.skip-ahead {:on-click skip-ahead} "⏭"]
           [:button.reset-speed  {:on-click reset-speed} "speed = 2x"]])))))

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
           (om/build time-component (:time state))
           (om/build previews-component state)
           (om/build add-world-component state)]))))

  !state
  {:target (. js/document (getElementById "console"))})

;;
;; Figwheel Client Initialization
;;

(fw/start
  {:websocket-url "ws://localhost:3449/figwheel-ws"})
