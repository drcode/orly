(ns orly.schemas
    (:require [schema.core :as sc]))

(def Rectangles [[(sc/one sc/Num "width") (sc/one sc/Num "height")]])

(def Contents [[(sc/maybe sc/Int)]])

(def Slices
  "Slices are a spactial indexing based on 'special' row and column dividers."
  {:rows     [sc/Num]
   :columns  [sc/Num]
   :contents Contents})

(def Point [(sc/one sc/Num "x") (sc/one sc/Num "y")])
(def Point-Index [(sc/one sc/Int "x-index") (sc/one sc/Int "y-index")])

(def Layout {:slices    Slices
             :positions {sc/Int Point}})

