(ns orly.rectangle-packing
    (:require [schema.core :as sc]
              [orly.schemas :refer [Rectangles Slices Layout Point-Index]]
              [orly.math :refer [round]]))

(defn insert-vec [v index item]
      "Insert an item into a vector at a given spot"
      (vec (concat (subvec v 0 index) [item] (subvec v index))))

(sc/defn sorted-int-index :- sc/Num [v :- [sc/Num] n :- sc/Num] 
         "finds int position in sorted list of ints."
         (loop [low 0
                high (count v)]
               (if (= low high)
                   low
                   (let [mid  (bit-shift-right (+ low high) 1)
                         vmid (v mid)]
                        (cond (= n vmid) mid
                              (> n vmid) (recur (inc mid) high)
                              :else      (recur low mid))))))

(sc/defn slices-cut-horizontal :- Slices [slices :- Slices n :- sc/Num]
         "Cut the slice data structure and mark all new regions with their contents"
         (let [{:keys [rows columns contents]} slices
               index                           (sorted-int-index rows n)
               outside-bounds                  (= index (count rows))]
              (cond outside-bounds     {:rows     (insert-vec rows index n)
                                        :columns  columns
                                        :contents (conj contents (vec (repeat (count columns) nil)))}
                    (= (rows index) n) slices
                    :else              {:rows     (insert-vec rows index n)
                                        :columns  columns
                                        :contents (insert-vec contents index (contents index))})))

(sc/defn slices-cut-vertical :- Slices [slices :- Slices n :- sc/Num]
         "Cut the slice data structure and mark all new regions with their contents"
         (let [{:keys [rows columns contents]} slices
               index                           (sorted-int-index columns n)
               outside-bounds                  (= index (count columns))]
              (cond outside-bounds     {:columns     (insert-vec columns index n)
                                        :rows  rows
                                        :contents (vec (map #(conj % nil) contents))}
                    (= (columns index) n) slices
                    :else              {:columns  (insert-vec columns index n)
                                        :rows  rows
                                        :contents (vec (map (fn [row-content]
                                                                (insert-vec row-content index (row-content index)))
                                                            contents))})))

(sc/defn slices-fill :- Slices [slices :- Slices x :- sc/Num y :- sc/Num width :- sc/Num height :- sc/Num id :- sc/Any]
         "Fills a rectangle in the slice index with an item"
         (let [{:keys [rows columns contents]} slices
               start-x                         (if (zero? x)
                                                   0
                                                   (inc (sorted-int-index columns x)))
               end-x                           (inc (sorted-int-index columns (+ x width)))
               start-y                         (if (zero? y)
                                                   0
                                                   (inc (sorted-int-index rows y)))
               end-y                           (inc (sorted-int-index rows (+ y height)))]
              (assoc slices
                     :contents
                     (reduce (fn [acc item]
                                 (assoc-in acc item id))
                             contents
                             (for [x (range start-x end-x) y (range start-y end-y)]
                                  [y x])))))

(sc/defn slices-width :- sc/Num [slices :- Slices]
         (or (last (:columns slices)) 0))

(sc/defn slices-height :- sc/Num [slices :- Slices]
         (or (last (:rows slices)) 0))

(sc/defn slices-add :- Slices [slices :- Slices x :- sc/Num y :- sc/Num w :- sc/Num h :- sc/Num id :- sc/Any]
         (-> slices
             (slices-cut-vertical (+ x w))
             (slices-cut-horizontal (+ y h))
             (slices-fill x y w h id)))

(sc/defn starting-layout :- Layout [rectangles :- Rectangles]
         "Places all rectangles in a horizontal strip"
         (let [sorted (sort-by (fn [[id [w h]]]
                                   (- (* w h)))
                               (map-indexed vector rectangles))]
              (reduce (fn [acc [id item]]
                          (let [{:keys [positions slices]} acc
                                [w h]                                   item
                                width (slices-width slices)]
                               (assoc acc
                                      :slices    (slices-add slices width 0 w h id)
                                      :positions (assoc positions id [width 0]))))
                      {:slices    {:rows     []
                                   :columns  []
                                   :contents []}
                       :positions {}}
                      sorted)))

(sc/defn slices-erase :- Slices [slices :- Slices x :- sc/Num y :- sc/Num w :- sc/Num h :- sc/Num] ;todo: erase obsolete horizontal slices
         "removes a rectangle from the slices data struct and does simplifications"
         (let [{:keys [contents rows columns] :as slices} (slices-fill slices x y w h nil)]
              (if (every? (fn [row]
                              (nil? (last row)))
                          contents)
                  {:rows rows
                   :columns (vec (butlast columns))
                   :contents (vec (map (comp vec butlast) contents))}
                  slices)))

(sc/defn clearances :- [sc/Bool] [contents :- [[sc/Any]] x-index :- sc/Num w-index :- sc/Num]
         "For every row slice at a given column, finds out if an item of width w-index could fit"
         (for [row contents]
              (every? not (subvec row x-index (+ x-index w-index)))))

(sc/defn clearances-by-height :- [[(sc/one sc/Num "y-index") (sc/one sc/Num "height")]] [lst :- [sc/Bool] rows :- [sc/Num]]
         "Takes a list of clearnces (true means cleared, false means not cleared) and the y position of all the slices, then bunches together neighboring slices into a single chunk. In this way, we can find out how much room there is, hieghtwise, in each hole. Note that an extra item will get added to the bottom with height=0 as a placeholder for downward expansion."
         (loop [lst          lst
                heights      (map - rows (cons 0 rows))
                clear-height 0
                clear-y      0
                cur-y        0
                acc          []]
               (if (seq lst)
                   (let [[cur & more] lst
                         [cur-height & more-heights] heights]
                        (if cur
                            (recur more more-heights (+ clear-height cur-height) clear-y (inc cur-y) acc)
                            (recur more
                                   more-heights
                                   0
                                   (inc cur-y)
                                   (inc cur-y)
                                   (if (zero? clear-height)
                                       acc
                                       (conj acc [clear-y clear-height])))))
                   (conj acc [clear-y clear-height]))))

(sc/defn best-bottom-spot :- Point-Index [slices :- Slices w :- sc/Num h :- sc/Num x-limit :- sc/Num]
         "scans the bottom of the layout for optimal spots at each column. Makes sure x remains less than x-limit (to prevent infinite loops)"
         (let [{:keys [rows columns contents]} slices
               zrows                           (vec (cons 0 rows))
               zcolumns                        (vec (cons 0 columns))
               x-limit-index                   (sorted-int-index zcolumns x-limit)]
              (apply min-key
                     second
                     (reverse (for [x-index (range 0 x-limit-index)]
                                   (let [x       (zcolumns x-index)
                                         w-index (count (take-while #(< % w) (map #(- % x) (subvec (vec (butlast zcolumns)) x-index))))] ;this is the width, as measured in # of columns instead of absolute units.
                                        [x-index (let [k  (clearances contents x-index w-index)
                                                       kh (clearances-by-height k rows)]
                                                      (if-let [[[y-index]] (seq (filter #(>= (second %) h) kh))]
                                                              y-index
                                                              (first (last kh))))]))))))

(sc/defn squish :- (sc/maybe Layout) [layout :- Layout rectangles :- Rectangles]
         "Moves the rightmost cube and packs it along the bottom"
         (let [{:keys [slices positions]} layout
               {:keys [contents]}         slices
               id                         (last (keep last contents))
               [x y]                      (positions id)]
              (when (pos? x)
                    (let [[w h]                           (rectangles id)
                          slices                          (slices-erase slices x y w h)
                          {:keys [rows columns contents]} slices
                          [x-index y-index]               (best-bottom-spot slices w h x)
                          x                               (nth (cons 0 columns) x-index)
                          y                               (nth (cons 0 rows) y-index)]
                         {:slices    (slices-add slices x y w h id)
                          :positions (assoc positions id [x y])}))))

(sc/defn layout :- Layout [rectangles :- Rectangles aspect-ratio :- sc/Num inspector]
         "Starts with a strip of rectangles, then repacks the rightmost rectangle until aspect ratio is reached."
         (loop [layt        (starting-layout rectangles)
                best-layout nil
                best-area   -1] ;have to use -1 instead of nil because of weird clojurescript type inference bug
               (let [{:keys [slices]} layt
                     width            (slices-width slices)
                     height           (slices-height slices)
                     ar               (/ width height)]
                    (inspector layt ar)
                    (if (< (/ width height) aspect-ratio)
                        ;;either the current or the previous best need to be compared for the best width:height tradeoff
                        (let [adj-width (* height aspect-ratio)
                              best-area best-area
                              area      (* height adj-width)]
                             (if (or (= best-area -1) (< area best-area))
                                 layt
                                 best-layout))
                        (let [adj-height              (/ width aspect-ratio)
                              area                    (* adj-height width)
                              [best-layout best-area] (if (or (= best-area -1) (< area best-area))
                                                          [layt area]
                                                          [best-layout best-area])]
                             (if-let [layt (squish layt rectangles)]
                                     (recur layt best-layout best-area)
                                     best-layout))))))

(sc/defn draw-layout [layout :- Layout rectangles :- Rectangles]
         (let [{:keys [slices positions]}      layout
               {:keys [rows columns contents]} slices
               heights                         (vec (map - rows (cons 0 rows)))
               widths                          (vec (map - columns (cons 0 columns)))]
                  (dotimes [y-index (count rows)]
                           (dotimes [_ (max 1 (round (heights y-index)))]
                                    (dotimes [x-index (count columns)]
                                             (dotimes [_ (max 1 (round (widths x-index)))]
                                                      (let [k (get-in contents [y-index x-index])]
                                                           (print (cond (not k) ".."
                                                                        (< k 10) (str " " k)
                                                                        :else k)))))
                                    (println)))))

