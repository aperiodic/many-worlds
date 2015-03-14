(ns many-worlds.core)

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
