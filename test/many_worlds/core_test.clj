(ns many-worlds.core-test
  (:require [clojure.test :refer :all]
            [many-worlds.core :refer :all]))

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
