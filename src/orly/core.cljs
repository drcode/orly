(ns orly.core
    (:require [goog.dom :as gdom]
              [om.next :as om :refer-macros [defui]]
              [om.dom :as dom]
              [orly.rectangle-packing :refer [layout]]
              [orly.rectangle-growing :refer [grow]]))

(defn get-div-dimensions
      "Get width and height of a div with a specified id."
      [e]
      (let [x (.-clientWidth e)
            y (.-clientHeight e)]
           {:width  x
            :height y}))

(def use-grow false)

(defn recalc-layout [component]
      (let [{:keys [width height]}                                                   (get-div-dimensions (dom/node component))
            rects                                                                    (:layout/rects (om/props component))
            sizes                                                                    (vec (for [{:keys [:rect/relwidth :rect/relheight]} rects] ;is vec needed?
                                                                                               [(max 1 relwidth) (max 1 relheight)]))
            {:keys [positions] {:keys [rows columns] :as slices} :slices :as layout} (layout sizes
                                                                                             (/ width height)
                                                                                             (fn [_ _]))
            sizes                                                                    (vec (grow (count rects) slices))
            layout-width                                                             (last columns)
            layout-height                                                            (last rows)
            scalex                                                                   (/ width layout-width)
            scaley                                                                   (/ height layout-height)
            scale                                                                    (min scalex scaley)]
           (om/transact! component
                         (vec (concat (map-indexed (fn [index {:keys [:db/id :rect/relwidth :rect/relheight] :as item}]
                                                       (let [[width height] (sizes index)
                                                             [x y] (positions index)]
                                                            `(app/update-rect ~(merge item
                                                                                      (if use-grow
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
                                                   rects)
                                      [{:main [{:rects [:rect/width :rect/height :rect/left :rect/top]}]}])
                              ))
           ) 1)

(defui Orly
       static om/IQuery
       (query [this]
              [{:layout/rects [:db/id :rect/relwidth :rect/relheight]} #_{:layout/settings [:orly/width :orly/height]}])
       Object
       (componentWillMount [this]
                           (let [resize-fn (fn []
                                               (recalc-layout this))]
                                (om/set-state! this resize-fn)
                                (.addEventListener js/window "resize" resize-fn)))
       (componentWillUpdate [this props _]
                            (js/setTimeout #(recalc-layout this) 1))
       (render [this]
               (let [children (om/children this)]
                    (apply dom/div
                           #js {:style #js {:height "100%"}}
                           children)))
       (componentDidMount [this]
                          (recalc-layout this))
       (componentWillUnmount [this]
                             (.removeEventListener js/window "resize" (om/get-state this))))



