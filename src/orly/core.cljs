(ns orly.core
    (:require [goog.dom :as gdom]
              [om.next :as om :refer-macros [defui]]
              [om.dom :as dom]
              [orly.rectangle-packing :as rp]
              [orly.rectangle-growing :as rg]))

(defn get-div-dimensions
      "Get width and height of a div with a specified id."
      [e]
      (let [x (.-clientWidth e)
            y (.-clientHeight e)]
           {:width  x
            :height y}))

(defn recalc-layout [component]
      (let [{:keys [width height]}                     (om/get-state component)
            rects                                      (:rects (om/props component))
            {:keys [grow after-transaction]}           (om/get-computed (om/props component))
            sizes                                      (vec (for [{:keys [:rect/relwidth :rect/relheight]} rects] ;is vec needed?
                                                                 [(max 1 relwidth) (max 1 relheight)]))
            {:keys [positions]
             {:keys [rows columns] :as slices} :slices
             :as layout}                               (rp/layout sizes
                                                                  (/ width height)
                                                                  (fn [_ _]))
             sizes                                     (vec (rg/grow (count rects) slices))
             layout-width                              (last columns)
             layout-height                             (last rows)
             scalex                                    (/ width layout-width)
             scaley                                    (/ height layout-height)
             scale                                     (min scalex scaley)]
           (om/transact! component
                         (vec (map-indexed (fn [index {:keys [:db/id :rect/relwidth :rect/relheight] :as item}]
                                               (let [[width height] (sizes index)
                                                     [x y] (positions index)]
                                                    `(app/update-rect ~(merge item
                                                                              (if grow
                                                                                  {:db/id id
                                                                                   :rect/left   (* scalex x)
                                                                                   :rect/top    (* scaley y)
                                                                                   :rect/width  (* scalex width)
                                                                                   :rect/height (* scaley height)}
                                                                                  {:db/id id
                                                                                   :rect/left   (* scale x) 
                                                                                   :rect/top    (* scale y)
                                                                                   :rect/width  (* scale relwidth)
                                                                                   :rect/height (* scale relheight)})))))
                                           rects)))
           (after-transaction)))

(defn update-dimensions [component]
      (let [{:keys [width height]} (get-div-dimensions (dom/node component))]
           (om/update-state! component assoc
                             :width width
                             :height height)))

(defui Orly
       static om/IQuery
       (query [this]
              [{:rects [:db/id :rect/relwidth :rect/relheight]}])
       Object
       (componentWillMount [this]
                           (let [resize-fn (fn []
                                            (update-dimensions this))]
                                (om/set-state! this {:resize-fn resize-fn})
                                (.addEventListener js/window "resize" resize-fn)))
       (componentWillUpdate [this _ _]
                            (js/setTimeout #(recalc-layout this) 1))
       (render [this]
               (let [children (om/children this)]
                    (apply dom/div #js {:style #js {:height "100%"}}
                           children)))
       (componentDidMount [this]
                          (update-dimensions this))
       (componentWillUnmount [this]
                             (.removeEventListener js/window "resize" (:resize-fn (om/get-state this)))))



