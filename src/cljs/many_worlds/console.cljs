(ns many-worlds.console
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [<! >! alts! chan close! put! timeout]]
            [clojure.string :as str]
            [figwheel.client :as fw]
            [om.core :as om]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;;
;; Utilities
;;

(defn frame-url
  [world-url t w]
   (str world-url "/frame.bmp"
        "?t=" (.toFixed t 2)
        (if w (str "&width=" w))))

(defn state-url
  [world-url]
  (str world-url "/state"))

(defn location
  []
  (-> (.-location js/document)
    str
    (str/replace #"/#?$" "")))

;;
;; Constants
;;

(def jump-delta-t
  "In order to calculate how far to jump around when skipping, multiply this
  delta by the current speed. With the default speed of 2 the jump will be twice
  this value, in seconds."
  1.0)

(def delta-t-ms
  "The time delta for previews; previews update at this interval in ms, and they
  advance by this interval in ms times the current speed."
  100)

;;
;; State
;;

(defonce !state
  (atom {:worlds [(location)]
         :time {:t 0, :speed 2}
         :preview-width 320}))

;;
;; Application Actions
;;

(defonce world-reset (chan 1))

(defonce world-resetter
  (go-loop []
    (let [state (<! world-reset)]
      (doseq [world (:worlds @!state)]
        (ajax/PUT (state-url world)
                  {:params state
                   :format {:content-type "application/edn"
                            :write identity}}))
      (recur))))

(defn reset-worlds
  "Reset the states of all worlds to use the state of the given world."
  [world-url]
  (ajax/GET (state-url world-url)
            {:response-format :raw
             :handler (fn [resp] (put! world-reset resp))}))

(defn add-world
  [state owner]
  (let [new-world (.-value (om/get-node owner "new-world"))]
    (when-not (empty? new-world)
      (om/transact! state :worlds #((fnil conj []) % new-world))
      (om/set-state! owner :text ""))))

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
      (let [t (get-in state [:time :t] 0)
            w (:preview-width state)]
        (html
          [:div.worlds
           (for [world (:worlds state)]
             [:div.world {:key world}
              [:a {:href "#", :on-click #(reset-worlds world)}
               [:img {:src (frame-url world t w)}]]])])))))

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

(defonce time-control (chan 1))

(defn time-component
  [{:keys [t speed] :as time} owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go-loop [[msg _] (alts! [time-control (timeout delta-t-ms)])]
        (when-not (= msg :terminate)
          (om/transact! time (fn [{:keys [speed t] :as time}]
                               (let [delta-t-s (/ delta-t-ms 1000.0)]
                                 (update-in time [:t] + (* speed delta-t-s)))))
          (recur (alts! [time-control (timeout delta-t-ms)] :priority true)))))

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
                                        (update-in time [:t] + (* speed jump-delta-t))))]
        (html
          [:div#time
           [:button.skip-back {:on-click skip-back} "⏮"]
           [:button.fast-backward {:on-click fb} "⏪"]
           [:span.time-display (str "t: " (.toFixed t 1))]
           [:span.speed-display (str "(" (.toFixed speed 1) "x)")]
           [:button.fast-forward {:on-click ff} "⏩"]
           [:button.skip-ahead {:on-click skip-ahead} "⏭"]
           [:button.reset-speed  {:on-click reset-speed} "speed = 2x"]])))

    om/IWillUnmount
    (will-unmount [_]
      (put! time-control :terminate))))

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
