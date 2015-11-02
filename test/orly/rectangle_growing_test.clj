(ns orly.rectangle-growing-test
  (:require [clojure.test :refer :all]
            [orly.rectangle-growing :refer :all]))

(deftest grow-content-horizontal-test
         (is (= [[0 0] [0 0] [1 1]]
                (grow-content-horizontal 2 [[0 0] [0 0] [1 1]])))
         (is (= [[0 0] [0 0] [1 1]]
                (grow-content-horizontal 2 [[0 0] [0 0] [1 nil]])))
         (is (= [[0 0] [0 0] [1 1]]
                (grow-content-horizontal 2 [[0 nil] [0 nil] [1 nil]])))
         (is (= [[0 0 0] [0 0 0] [1 1 1]]
                (grow-content-horizontal 2 [[0 0 0] [0 0 0] [1 nil nil]]))))

(deftest grow-content-test
         (is (= [[0 0 0] [0 0 0] [1 1 1] [1 1 1]]
                (grow-content 2 [[0 0 nil] [0 0 nil] [1 1 nil] [nil nil nil]])))
         (is (= [[0 0 0] [0 0 0] [1 1 1] [1 1 1]]
                (grow-content 2 [[nil 0 0] [nil 0 0] [nil 1 1] [nil nil nil]])))
         (is (= [[0 0 0] [1 1 1] [2 2 2]]
                (grow-content 3 [[0 nil nil] [nil 1 nil] [nil nil 2]]))))

(deftest grow-test
         (is (= [[2 2] [2 1]]
                (grow 2
                      {:rows     [1 2 3],
                       :columns  [1 2],
                       :contents [[0 0] [0 0] [1 nil]]},)))
         (is (= [[3 1] [3 1] [3 1]]
                (grow 3
                      {:rows     [1 2 3],
                       :columns  [1 2 3],
                       :contents [[0 nil nil] [nil 1 nil] [nil nil 2]]}))))
