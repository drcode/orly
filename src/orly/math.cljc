(ns orly.math
    (:require [schema.core :as sc]
              #?(:clj [clojure.math.numeric-tower :as nt])))

#?(:cljs (sc/defn round :- sc/Int [n :- sc/Num]
                  (js/Math.round n)))

#?(:clj (def round nt/round))

(sc/defn transpose :- [[sc/Any]] [matrix :- [[sc/Any]]]
      (apply mapv vector matrix))

(sc/defn avg :- sc/Num [& lst :- [sc/Num]]
         (/ (apply + lst) (count lst)))
