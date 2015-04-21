(ns many-worlds.core
  (:require [many-worlds.api :as api]
            [qutils.animation :refer [animation]]
            [qutils.curve :as curve]
            [qutils.vector :as vec]
            [ring.adapter.jetty :refer [run-jetty]]))

(def bezier-order 4)

;; this is not intended to be synchronized with the state of the bezier walk
(def !api-state (atom {:server nil, :starter-id nil, :stopper-id nil}))

(def defaults
  {;; The random bezier walk is composed of cubic bezier curve segments. This
   ;; value defines how long it takes the walk to traverse each segment. The
   ;; value's units end up being whatever unit the `t` argument of `position-at`
   ;; is in, but the default value works best if `t` is in seconds.
   :segment-length 2
   ;; The value to use for each segment of the default min-point. Put another
   ;; way, the default min-point is constructed by creating an n-vector with
   ;; each segment set to this value.
   :min 0
   ;; The value to use for each segment of the default max-point. Put another
   ;; way, the default max-point is constructed by creating an n-vector with
   ;; each segment set to this value.
   :max 1
   :port 3000})

(defn rand-control-points
  ([n min max] (rand-control-points n min max (vec/rand-point min max)))
  ([n min max start]
   (concat [start] (repeatedly (dec n) #(vec/rand-point min max)))))

(defn stop-server!
  []
  (if-let [server (:server @!api-state)]
    (let [me (.getId (java.lang.Thread/currentThread))
          {:keys [stopper-id]} (swap! !api-state update-in [:stopper-id] (fnil identity me))]
      (when (= stopper-id me)
        (swap! !api-state
               (fn [{:keys [server]}]
                 (when server (.stop server))
                 {:server nil, :stopper-id nil, :starter-id nil}))))))

(defn start-server!
  [handler port]
  (stop-server!)
  (when handler
    (let [me (.getId (java.lang.Thread/currentThread))
          {:keys [starter-id]} (swap! !api-state update-in [:starter-id] (fnil identity me))]
      (when (= starter-id me)
        (let [server (run-jetty handler {:port port, :join? false})]
          (swap! !api-state assoc :server server :starter-id nil :stopper-id nil))))))

(defn setup!
  "Initialize a random bezier walk through an `n` dimensional state space,
  storing the walk's state in the provided atom (which will need to be passed to
  the position-at function). The optional third argument can be a map containing
  options keywords to change some characteristics of the random bezier walk
  and the Many Worlds API server. The relevant keys are:

    - :segment-length
      The random bezier walk is composed of cubic bezier curve segments. This
      value defines how long it takes the walk to traverse each segment. The
      value's units end up being whatever unit the `t` argument of `position-at`
      is in, but the default value of 2 works best if `t` is in seconds.

    - :min-point
    - :max-point
      The random bezier walk is confined to the box between `:min-point`
      (inclusive) and `:max-point` (exclusive). Points are n-vectors of numbers.

    - :min
    - :max
      If `:min-point` or `:max-point` is not defined, then it will be
      constructed by creating an n-vector with each segment set to `:min` or
      `:max` as appropriate. The default `:min` value is 0, and the default
      `:max` value is 1.

    - :port
      The port on which to run the Many Worlds API server. The API server is
      used to obtain frames of the sketch at some given time `t`, to serve the
      current state of the bezier walk, and to reset the bezier walk state with
      a state submitted in a request body.

  In addition to the options in the options map, you can also pass five
  additional quil-related arguments. If all of these arguments are provided,
  then the Many Worlds API will be started.

  These arguments are:

    - `sketch-var`
      The var you pass to quil.core/defsketch that will hold your sketch.
      This should be the var itself to avoid a cyclic dependency between your
      sketch and its setup function (wherein you call this function).
      This is needed because Many Worlds needs the sketch applet in order to be
      able to render a frame of the sketch.

    - `width` and `height`
      Integers defining the nominal width and height of your sketch.

    - `configure-quil!`
      A function of no arguments that configures the global state of quil, such
      as enabling smoothing or setting the color mode. It should not do any
      other sort of setup such as loading data. This is needed because a new

    - `draw`
      A function that draws a frame of your sketch, and takes one argument: the
      time of the frame to draw, in seconds. The only other piece of state this
      function should use is the vector returned by `position-at`.
  "
  ([n state-atom] (setup! n state-atom {}))
  ([n state-atom options] (setup! n state-atom options nil nil nil nil nil))
  ([n state-atom options sketch-var width height configure-quil! draw]

   (let [{:keys [segment-length min-point max-point min max port]} (merge defaults options)
         min-point (or min-point (vec (repeat n min)))
         max-point (or max-point (vec (repeat n max)))
         handler (if (and sketch-var width height configure-quil! draw)
                   (api/handler state-atom sketch-var width height configure-quil! draw))]

     (when-not (= n (count min-point) (count max-point))
       (throw (IllegalArgumentException. (str "inconsistent dimensions between `n`, `min-point`,"
                                              " and `max-point`."))))
     (when-not (number? min)
       (throw (IllegalArgumentException. "`min` option must be a number.")))
     (when-not (number? max)
       (throw (IllegalArgumentException. "`max` option must be a number.")))

     (when handler (start-server! handler port))

     (let [ctrl-pts (rand-control-points bezier-order min-point max-point)
           first-segment (animation (curve/bezier ctrl-pts) 0 segment-length)]
       (reset! state-atom {:path (sorted-map 0 first-segment)
                           :n n :segment-length segment-length
                           :max-point max-point, :min-point min-point})))))

(defn curve-for-t
  "If there is a segment of the path in `state` that encompasses `t`, return
  that segment. If not, return nil."
  [state t]
  (if state
    (let [{:keys [path segment-length]} state]
      (get path (-> (quot t segment-length) (* segment-length) int)))))

(defn backfill-segments
  "This produces a derived state after augmenting the path with all the segments
  that will fit between the highest key in the state's path and the `until`
  time (inclusive)."
  [state until]
  (let [{:keys [path n segment-length min-point max-point]} state
        new-seg (fn [t p0]
                  (let [pts (rand-control-points bezier-order min-point max-point p0)]
                    (animation (curve/bezier pts) t segment-length)))
        assoc-new-seg (fn [path t]
                        (let [last-seg (get path (last (keys path)))
                              p0 (-> last-seg :curve :points last)]
                          (assoc path t (new-seg t p0))))
        t0 (last (keys path))]
    (update-in state [:path] #(reduce assoc-new-seg
                                      %
                                      (range (+ t0 segment-length)
                                             (inc until)
                                             segment-length)))))

(defn position-at
  "Return the position vector of the random bezier walk described by the atom
  `state` at time `t`, unless `state` has not been initialized by passing it to
  `setup!`, in which case nil is returned instead. If `t` is less than zero, the
  return value will be nil.

  Note that this backfills any missing bezier path segments in `state`. If
  you're fast-forwarding by giving successive `t`s with stride larger than the
  state's configured segment-length, not only will you not have a smooth
  animation, you'll be potentially allocating many animations each frame, which
  will not be freed until `state` is reset or goes out of scope."
  [state t]
  (cond
    (neg? t) nil
    (curve-for-t @state t) (let [bez (curve-for-t @state t)] (bez t))
    :else (let [bez (curve-for-t (swap! state backfill-segments t) t)]
            (bez t))))
