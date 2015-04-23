(ns many-worlds.example.dejong
  (:gen-class)
  (:require [many-worlds.api :as api]
            [many-worlds.core :as worlds]
            [quil.core :as q]))

;;
;; Constants
;;

(def pi Math/PI)
(def -pi (* -1.0 Math/PI))

(def w 1024)
(def h 768)

(def w2 (/ w 2))
(def h2 (/ h 2))
(def w4 (/ w 4))
(def h4 (/ h 4))

(def frame-rate 30)

;;
;; De Jong
;;

(defn de-jong-fn
  [a b c d]
  (fn [[x y]]
    [(- (Math/sin (* a y)) (Math/cos (* b x)))
     (- (Math/sin (* c x)) (Math/cos (* d y)))]))

;;
;; Sketch
;;

(defn quil-setup!
  []
  (q/smooth)
  (q/frame-rate frame-rate))

(declare dejong-sketch)
(declare draw)

(def !walk-state (atom nil))

(defn setup!
  [port]
  (worlds/setup! 4 !walk-state
                 {:min -pi, :max pi, :segment-length 20, :port port}
                 #'dejong-sketch w h quil-setup! draw)
  (quil-setup!))

(defn rand-starting-point
  "Returns a random point in the box between [-2, -2] and [2, 2]."
  []
  [(- 2 (rand 4)) (- 2 (rand 4))])

(defn draw
  [t]
  (let [[a b c d] (worlds/position-at !walk-state t)
        de-jong (de-jong-fn a b c d)]
    (q/background 255 252 226)
    (q/with-translation [w2 h2]
      (q/no-stroke)
      (q/fill 0 128)
      (doseq [starting-pt (repeatedly 100 rand-starting-point)
              [x y] (take 100 (iterate de-jong starting-pt))]
        (q/rect (dec (* h4 x)) (dec (* h4 y)) 2 2)))))

(defn dejong
  [port]
  (q/defsketch dejong-sketch
    :title "De Jong Worlds"
    :size [w h]
    :setup #(setup! port)
    :draw (fn [] (let [t (/ (q/frame-count) (* 1.0 frame-rate))]
                   (draw t)))))

(defn -main
  [& [port-arg & _]]
  (let [port (Integer/parseInt (or port-arg "3000"))]
    (dejong port)))

(comment
  ;; start the sketch
  (dejong 3000)

  ;; stop the many worlds API server
  (api/stop-server!))
