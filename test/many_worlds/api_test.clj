(ns many-worlds.api-test
  (:require [clojure.test :refer :all]
            [many-worlds.api :refer :all]))

(deftest parse-frame-opts-test
  (is (= {:s 1 :t 0} (parse-frame-opts {})))
  (is (= {:s 1 :t 0} (parse-frame-opts {:s "foo" :t "bar"})))

  (is (= {:s 1/2 :t 0} (parse-frame-opts {:s "50"})))
  (is (= {:s 57/100 :t 0} (parse-frame-opts {:s "57"})))

  (is (= {:s 1 :t 100} (parse-frame-opts {:t "100"})))
  (is (= {:s 1 :t 127.1} (parse-frame-opts {:t "127.1"})))
  (is (= {:s 1 :t 12}
         (parse-frame-opts {:t "latest"} {:path (sorted-map 12 :animation)}))))
