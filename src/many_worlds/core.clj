(ns many-worlds.core
  (:require [qutils.animation :refer [animation]]
            [qutils.curve :as curve]
            [qutils.vector :as vec]))

(def ^:private !state (atom nil))

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
   :max 1})

(defn sample-control-points
  [n min-point max-point]
  (repeatedly n #(vec/rand-point min-point max-point)))

(defn setup!
  "Initialize a random bezier walk through an `n` dimensional state space. The
  optional second argument can be a map containing options keywords to change
  some characteristics of the random bezier walk. The relevant keys are:

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
  "
  ([n] (setup! n {}))
  ([n options]
   (let [{:keys [segment-length min-point max-point min max]} (merge defaults options)
         min-point (or min-point (vec (repeat n min)))
         max-point (or max-point (vec (repeat n max)))]

     (when-not (= n (count min-point) (count max-point))
       (throw (IllegalArgumentException. (str "inconsistent dimensions between `n`, `min-point`,"
                                              " and `max-point`."))))
     (when-not (number? min)
       (throw (IllegalArgumentException. "`min` option must be a number.")))
     (when-not (number? max)
       (throw (IllegalArgumentException. "`max` option must be a number.")))

     (let [ctrl-pts (sample-control-points n min-point max-point)
           first-segment (animation (curve/bezier ctrl-pts) 0 segment-length)]
       (reset! !state {:path (sorted-map 0 first-segment)
                       :n n :segment-length segment-length
                       :max-point max-point, :min-point min-point})))))

(defn curve-for-t
  "If there is a segment of the path in `state` that encompasses `t`, return
  that segment. If not, return nil."
  [state t]
  (if state
    (let [{:keys [path segment-length]} state]
      (get path (-> (quot t segment-length) (* segment-length) int)))))
