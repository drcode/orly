(ns orly.rectangle-growing
    (:require [schema.core :as sc]
              [orly.schemas :refer [Rectangles Slices Contents]]
              [orly.math :refer [transpose]]))

(sc/defn grow-content-horizontal :- Contents [num :- sc/Int contents :- Contents]
      "grows the content to the right. There are 'num' different items."
      (let [k        (conj (vec (mapcat (partial cons -1) contents)) -1)
            growable (reduce (fn [acc [left right :as item]]
                                 (if (and (not= left right) right)
                                     (disj acc left)
                                     acc))
                             (set (range num))
                             (map vector k (rest k)))]
           (if (seq growable)
               (recur num
                      (mapv (fn [row]
                                (mapv (fn [left cur]
                                          (if (and (not cur) (growable left))
                                              left
                                              cur))
                                      (cons -1 row)
                                      row))
                            contents))
               contents)))

(sc/defn grow-content :- Contents [num :- sc/Int contents :- Contents]
         "Can grow content both in all four direction"
         (let [hmirror (fn [contents]
                           (mapv (comp vec reverse) contents))
               gch     (partial grow-content-horizontal num)]
              (-> contents
                  gch
                  hmirror
                  gch
                  hmirror
                  transpose
                  gch
                  hmirror
                  gch
                  hmirror
                  transpose)))

(sc/defn grow :- Rectangles [num :- sc/Int slices :- Slices]
         "Takes a bunch of rectangle sizes and a layout for them. Returns a new layout with the items grown as big as possible."
         (let [{:keys [rows columns contents]} slices
               contents                        (grow-content num contents)]
              (for [{:keys [left right top bottom] :as dimensions} (reduce (fn [acc [x y :as item]]
                                                                               (if-let [id (get-in contents [y x])] 
                                                                                       (let [max-x    (columns x)
                                                                                             max-y    (rows y)
                                                                                             min-x    (if (zero? x)
                                                                                                          0
                                                                                                          (columns (dec x)))
                                                                                             min-y    (if (zero? y)
                                                                                                          0
                                                                                                          (rows (dec y)))
                                                                                             nullsafe (fn [f a b]
                                                                                                          (if a
                                                                                                              (f a b)
                                                                                                              b))]
                                                                                            (update-in acc [id]
                                                                                                       (fn [{:keys [left right top bottom]}]
                                                                                                           {:left   (nullsafe min left min-x)
                                                                                                            :top    (nullsafe min top min-y)
                                                                                                            :right  (nullsafe max right max-x)
                                                                                                            :bottom (nullsafe max bottom max-y)})))
                                                                                       acc))
                                                                           (vec (repeat num {}))
                                                                           (for [y (range (count rows)) x (range (count columns))]
                                                                                [x y]))]
                   [(- right left) (- bottom top)])))
