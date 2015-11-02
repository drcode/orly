(ns orly.math-test
    (:require [clojure.test :refer :all]
              [orly.math :refer :all]))

(deftest round-test
         (is (= 2 (round 2.4))))

(deftest transpose-test
         (is (= [[1 2 3] [4 5 6]] (transpose [[1 4] [2 5] [3 6]]))))

(deftest avg-test
         (is (= 5 (avg 1 8 2 9))))
