(ns orly.rectangle-packing-test
  (:require [clojure.test :refer :all]
            [orly.rectangle-packing :refer :all]))

(deftest insert-vec-test
         (is (= [:a :b :c] (insert-vec [:a :c] 1 :b)))
         (is (= [:a :c :b] (insert-vec [:a :c] 2 :b))))

(deftest sorted-int-index-test
         (is (= 0
                (sorted-int-index [1 3 4 5.5 6 7] 1)))
         (is (= 1
                (sorted-int-index [1 3 4 5.5 6 7] 3)))
         (is (= 2
                (sorted-int-index [1 3 4 5.5 6 7] 4)))
         (is (= 3
                (sorted-int-index [1 3 4 5.5 6 7] 5.5)))
         (is (= 4
                (sorted-int-index [1 3 4 5.5 6 7] 6)))
         (is (= 5
                (sorted-int-index [1 3 4 5.5 6 7] 7))))

(deftest slices-cut-horizontal-test
         (is (= {:rows [4 10]
                 :columns [20]
                 :contents [[3] [3]]} 
                (slices-cut-horizontal {:rows     [10]
                                       :columns  [20]
                                       :contents [[3]]}
                                       4)))
         (is (= {:rows     [10 20]
                 :columns  [20]
                 :contents [[3] [nil]]} 
                (slices-cut-horizontal {:rows     [10]
                                       :columns  [20]
                                       :contents [[3]]}
                                       20)))
         (is (= {:rows [4 10]
                 :columns [20]
                 :contents [[3] [3]]}
                (slices-cut-horizontal {:rows     [4 10]
                                        :columns  [20]
                                        :contents [[3] [3]]}
                                       4)))
         (is (= {:rows [1 2 3],
                 :columns [1 2 3],
                 :contents [[0 0 1] [0 0 nil] [nil nil nil]]}
                (slices-cut-horizontal {:columns [1 2 3], :rows [1 2], :contents [[0 0 1] [0 0 nil]]}
                                       3))))

(deftest slices-cut-vertical-test
         (is (= {:rows [10]
                 :columns [4 20]
                 :contents [[3 3]]} 
                (slices-cut-vertical {:rows     [10]
                                       :columns  [20]
                                       :contents [[3]]}
                                       4)))
         (is (= {:rows     [10]
                 :columns  [20 30]
                 :contents [[3 nil]]} 
                (slices-cut-vertical {:rows     [10]
                                       :columns  [20]
                                       :contents [[3]]}
                                       30)))
         (is (= {:rows     [10]
                 :columns  [4 20]
                 :contents [[3] [3]]}
                (slices-cut-vertical {:rows     [10]
                                        :columns  [4 20]
                                        :contents [[3] [3]]}
                                       4))))

(deftest slices-fill-test
         (is (= {:rows [1 2]
                 :columns [2 3]
                 :contents [[0 1] [0 nil]]}
                (slices-fill {:rows [1 2]
                 :columns [2 3]
                              :contents [[0 nil] [0 nil]]}
                             2
                             0
                             1
                             1
                             1))))

(deftest slices-add-test
         (is (= {:rows [1 2 3],
                 :columns [1 2 3],
                 :contents [[0 0 1] [0 0 nil] [2 nil nil]]}
                (slices-add {:rows     [1 2]
                             :columns  [2 3]
                             :contents [[0 1] [0 nil]]}
                            0
                            2
                            1
                            1
                            2))))

(deftest starting-layout-test
         (is (= {:positions {0 [0 0], 1 [2 0]}
                 :slices    {:rows [1 2], :columns [2 3], :contents [[0 1] [0 nil]]}}
                (starting-layout [[2 2] [1 1]])))
         (is (= {:positions {0 [100 0], 1 [0 0]}
                 :slices    {:rows [1 2],
                             :columns [100 101],
                             :contents [[1 0] [nil 0]]}}
                (starting-layout [[1 2] [100 1]])))
         (is (= {:positions {}
                 :slices    {:rows [], :columns [], :contents []}}
                (starting-layout []))))

(deftest clearances-test
         (is (= [false false]
                (clearances [[0 1] [0 nil]] 0 2)))
         (is (= [false true]
                (clearances [[0 1] [0 nil]] 1 1)))
         (is (= [false false]
                (clearances [[0 1 2] [0 nil 2]] 1 2)))
         (is (= [false false]
                (clearances [[0 1 2] [0 nil 2]] 1 2))))

(deftest clearances-by-height-test
         (is (= [[0 5] [3 5]] (clearances-by-height [true true false true] [1 5 7 12])))
         (is (= [[1 4] [3 5]] (clearances-by-height [false true false true] [1 5 7 12])))
         (is (= [[1 4] [4 0]] (clearances-by-height [false true false false] [1 5 7 12])))
         (is (= [[0 12]] (clearances-by-height [true true true true] [1 5 7 12]))))

(deftest best-bottom-spot-test
         (is (= [0 2]
                (best-bottom-spot {:rows [1 2], :columns [2 3], :contents [[0 1] [0 nil]]} 1 1 2)))
         (is (= [1 1]
                (best-bottom-spot {:rows [1 2], :columns [2 3], :contents [[0 1] [0 nil]]} 1 1 3)))
         (is (= [1 1]
                (best-bottom-spot {:rows [1 2], :columns [2 3], :contents [[0 1] [0 nil]]} 2 2 3)))
         (is (= [1 1]
                (best-bottom-spot {:rows [1 2], :columns [1 2 3], :contents [[0 1 2] [0 nil 2]]} 1 1 3)))
         (is (= [1 2]
                (best-bottom-spot {:rows [1 2 3], :columns [1 2 3], :contents [[0 1 2] [0 nil 2] [0 nil nil]]} 2 1 3)))
         (is (= [0 2]
                (best-bottom-spot {:rows [1 2 3], :columns [1 2 3], :contents [[0 1 2] [0 nil 2] [nil nil 1]]} 2 1 3)))
         (is (= [0 3]
                (best-bottom-spot {:rows [1 2 3], :columns [1 2 3], :contents [[0 1 2] [0 nil 2] [0 nil 1]]} 2 1 3)))
         (is (= [1 1]
                (best-bottom-spot {:rows [1 2 3], :columns [1 2 3], :contents [[0 1 2] [0 nil 2] [0 nil 1]]} 1 3 3))))


(deftest squish-test
         (is (= {:slices    {:rows     [1 2 3],
                             :columns  [1 2],
                             :contents [[0 0] [0 0] [1 nil]]},
                 :positions {0 [0 0], 1 [0 2]}}
                (squish {:slices    {:rows [1 2] :columns [2 3] :contents [[0 1] [0 nil]]}
                         :positions {0 [0 0] 1 [2 0]}}
                        [[2 2] [1 1]])))
         (is (= {:slices    {:rows     [1 2 3],
                             :columns  [1 2],
                             :contents [[0 nil] [1 1] [1 1]]},
                 :positions {0 [0 0], 1 [0 1]}}
                (squish {:slices    {:rows [1 2] :columns [1 3] :contents [[0 1] [nil 1]]}
                         :positions {0 [0 0] 1 [1 0]}}
                        [[1 1] [2 2]])))
         (is (= {:slices    {:rows     [1 2 3],
                             :columns  [1 2 3],
                             :contents [[0 0 1] [0 0 nil] [2 nil nil]]},
                 :positions {0 [0 0], 1 [2 0], 2 [0 2]}}
                (squish {:slices    {:rows [1 2] :columns [2 3] :contents [[0 1] [0 2]]}
                         :positions {0 [0 0] 1 [2 0] 2 [2 1]}}
                        [[2 2] [1 1] [1 1]])))
         ;; if squishing isn't possible it'll return nil
         (is (= nil
                (squish {:slices    {:rows     [2],
                                     :columns  [2],
                                     :contents [[0]]},
                         :positions {0 [0 0]}}
                        [[2 2]]))))

(deftest layout-test
         (is (= {:slices
                 {:rows     [1 2 3],
                  :columns  [1 2],
                  :contents [[0 0] [0 0] [1 nil]]},
                 :positions {0 [0 0], 1 [0 2]}}
                (layout [[2 2] [1 1]] 0.1 (fn [_ _]))))
         (is (= {:slices    {:rows [1 2], :columns [2 3], :contents [[0 1] [0 nil]]},
                 :positions {0 [0 0], 1 [2 0]}}
                (layout [[2 2] [1 1]] 10 (fn [_ _]))))
         (is (= {:slices
                 {:rows [3 6 9],
                  :columns [3 6],
                  :contents [[0 0] [0 0] [1 nil]]},
                 :positions {0 [0 0], 1 [0 6]}}
                (layout [[6 6] [3 3]] 0.5 (fn [_ _])))))

;;Enable this code to generate some random rectangles and see the resulting layouts
#_(dotimes [_ 20] (let [rectangles (vec (repeatedly 20 (fn [] [(inc (rand-int 10)) (inc (rand-int 10))])))
                 aspect-ratio (float (/ (inc (rand-int 10)) (inc (rand-int 10))))]
                (println)
                (println "target aspect ratio=" aspect-ratio)
                (let [result (layout rectangles
                                     aspect-ratio
                                     (fn [l ar]
                                         #_(println)
                                         #_(println "aspect ratio=" (float ar))
                                         #_(draw-layout l rectangles)))]
                     (println)
                     (println "best compromise:")
                     (draw-layout result rectangles))))




