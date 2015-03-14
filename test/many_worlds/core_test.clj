(ns many-worlds.core-test
  (:require [clojure.test :refer :all]
            [many-worlds.core :refer :all]
            [qutils.vector :refer [dist]]))

(deftest setup!-test
  (is (= {:n 3, :segment-length 2, :min-point [0 0 0], :max-point [1 1 1]}
         (-> (setup! 3) (dissoc :path))))

  (is (= {:n 2, :segment-length 4, :max-point [2 2] :min-point [-2 -2]}
         (-> (setup! 2 {:segment-length 4 :max 2 :min -2})
           (dissoc :path))))

  (is (= {:n 2, :segment-length 4, :max-point [2 2] :min-point [-2 -2]}
         (-> (setup! 2 {:segment-length 4 :max-point [2 2] :min-point [-2 -2]})
           (dissoc :path))))

  (is (thrown? IllegalArgumentException (setup! 3 {:min-point [0 0]})))
  (is (thrown? IllegalArgumentException (setup! 3 {:max-point [0 0]})))
  (is (thrown? IllegalArgumentException (setup! 2 {:min [0 0]})))
  (is (thrown? IllegalArgumentException (setup! 2 {:max [0 0]}))))

(deftest curve-for-t-test
  (let [mk-state (fn [t seg-length]
                   {:segment-length seg-length
                    :path (reduce (fn [path t] (assoc path t :curve))
                                  (sorted-map)
                                  (range 0 t seg-length))})]
    (is (= :curve (curve-for-t (mk-state 1 2) 1)))
    (is (= :curve (curve-for-t (mk-state 13 2) 13)))
    (is (nil? (curve-for-t (mk-state 1 2) 3)))
    (is (nil? (curve-for-t nil 3)))))

(deftest position-at-test
  (let [!state @#'many-worlds.core/!state]
    (testing "nil returned when state is unitialized"
      (reset! !state nil)
      (is (nil? (position-at 1)))
      (is (nil? (position-at 11))))

  (testing "nil returned when t is negative"
    (setup! 3)
    (is (nil? (position-at -1)))
    (is (nil? (position-at -11))))

  (let [seg-length 2
        normalized-three-vec? (fn [x]
                                (and (vector? x)
                                     (= (count x) 3)
                                     (every? number? x)))]
    (setup! 3 {:segment-length seg-length})
    (let [p (position-at 0.5)]
      (is (normalized-three-vec? p))
      (is (normalized-three-vec? (position-at 10)))

      (testing "backfilling is performed"
        (doseq [t (range 0 10.1 seg-length)]
          (is (curve-for-t @!state t))))

      (testing "backfilling doesn't change extant path segments"
        (is (= p (position-at 0.5))))

      (testing "segments start and end close together"
        (doseq [t (range 0 10.1 seg-length)
                :let [t-prev (- t 0.01)
                      t-next (+ t 0.01)
                      p-prev (position-at t-prev)
                      p-next (position-at t-next)]]
          (is (< (dist p-prev p-next) 0.1))))))))
