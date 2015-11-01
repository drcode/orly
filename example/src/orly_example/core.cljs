(ns orly-example.core
    (:require [goog.dom :as gdom]
              [om.next :as om :refer-macros [defui]]
              [om.dom :as dom]
              [orly.core :as o]
              [datascript.core :as d]))

(def conn (d/create-conn {}))

(d/transact! conn
             [{:db/id -1
               :rect/color "purple"
               :rect/relwidth 3
               :rect/relheight 2}
              {:db/id -2
               :rect/color "orange"
               :rect/relwidth 2
               :rect/relheight 2}
              {:db/id -3
               :rect/color "yellow"
               :rect/relwidth 1
               :rect/relheight 1}
              {:db/id -4
               :rect/color "pink"
               :rect/relwidth 1
               :rect/relheight 1}
              {:db/id -5
               :rect/color "navy"
               :rect/relwidth 1
               :rect/relheight 1}
              {:db/id -6
               :rect/color "silver"
               :rect/relwidth 1
               :rect/relheight 1}])

(enable-console-print!)

(defn read-rects [state selector]
      (d/q '[:find [(pull ?e ?selector) ...]
             :in $ ?selector 
             :where [?e :rect/color ?color]]
           (d/db state)
           selector))

(defmulti read om/dispatch)

(defmethod read :proplist [{:keys [state selector] :as env} _ _]
           {:value {:properties (read-rects state (:properties (first selector)))}})

(defmethod read :main [{:keys [state selector] :as env} _ _]
           (let [k (set (distinct (apply concat (map keys selector))))
                 zup {:value {}}
                 zup (if (k :layout)
                         (assoc-in zup [:value :layout] {:layout/rects (read-rects state [:db/id :rect/relwidth :rect/relheight])})
                                                  zup
)
                 zup (if (k :rects)
                         (assoc-in zup [:value :rects] (read-rects state [:db/id :rect/color :rect/width :rect/height :rect/left :rect/top]))
                         zup)]
                zup))

(defmulti mutate om/dispatch)

(defmethod mutate 'app/update-rect
           [{:keys [state]} _ rect]
           {:value []
            :action (fn []
                        (d/transact! state
                                     [rect])
                        )})

(defui Property
       static om/IQuery
       (query [this]
              [:db/id :rect/relwidth :rect/relheight])
       Object
       (render [this]
               (let [entity   (om/props this)
                     prop-row (fn [key text]
                                  (let [twiddle (fn [positive?]
                                                    (dom/span #js {:style #js {:backgroundColor "black"
                                                                               :borderRadius "0.2em"
                                                                               :padding-left "0.2em"
                                                                               :padding-right "0.2em"
                                                                               :color "white"}
                                                                   :onClick (fn []
                                                                                (om/transact! this `[(app/update-rect ~(assoc entity
                                                                                                                              key (if positive?
                                                                                                                                      (inc (key entity))
                                                                                                                                      (max 1 (dec (key entity))))))
                                                                                                     {:main [{:layout [{:layout/rects [:rect/relwidth :rect/relheight]}]}] }]))
                                                                   }
                                                              (if positive?
                                                                       "+"
                                                                       "-")))]
                                       (dom/div #js {:style #js {:padding "0.2em"
                                                                 :padding-right "1.8em"
                                                                 :backgroundColor "silver"}}
                                                text
                                                " "
                                                (twiddle false)
                                                " "
                                                (key entity)
                                                " "
                                                (twiddle true))))]
                    (dom/div #js {:style #js {:textAlign "right"}}
                             (prop-row :rect/relwidth "Width")
                             (prop-row :rect/relheight "Height")
                             (dom/div #js {:style #js {:height          "0.2em"
                                                       :backgroundColor "grey"}})))))

(def property (om/factory Property {:keyfn :db/id}))

(defui PropertyList
       static om/IQuery
       (query [this]
              [{:properties (om/get-query Property)}])
       Object
       (render [this]
               (dom/div #js {:style #js {:position        "absolute"
                                         :top             0
                                         :left            0
                                         :width           "10em"
                                         :height          "100%"
                                         :backgroundColor "grey"
                                         :textAlign      "center"}}
                        (dom/div #js {:style #js {:backgroundColor "grey"
                                                  :padding "0.5em"
                                                  :color           "white"}}
                                 "Relative Dimensions")
                        (for [p (:properties (om/props this))]
                             (property p)))))

(def property-list (om/factory PropertyList))

(defui Child
       static om/IQuery
       (query [this]
              [:db/id :rect/color :rect/width :rect/height :rect/left :rect/top])
       Object
       (render [this]
               (let [{:keys [:db/id :rect/color :rect/width :rect/height :rect/top :rect/left]} (om/props this)]
                    (dom/div #js {:style #js {:backgroundColor color
                                              :position         "absolute"
                                              :width            (or width 0)
                                              :height           (or height 0)
                                              :left             (or left 0)
                                              :top              (or top 0)}}
                             color))))

(def child (om/factory Child {:keyfn :db/id}))

(def orly (om/factory o/Orly))

(defui MainArea
       static om/IQuery
       (query [this]
              [{:layout (om/get-query o/Orly)} {:rects (om/get-query Child)}])
       Object
       (render [this]
               (dom/div #js {:style #js {:position "absolute"
                                         :top      0
                                         :left     "10em"
                                         :right    0
                                         :height   "100%"}}
                        (dom/div #js {:style #js {:position        "absolute"
                                                  :top             "3em"
                                                  :left            "3em"
                                                  :right           "3em"
                                                  :bottom          "3em"
                                                  :backgroundColor "grey"}}
                                 (let [{:keys [rects layout]} (om/props this)]
                                      (orly layout
                                            (for [item rects]
                                                 (child item))))))))

(def main-area (om/factory MainArea))

(defui App
       static om/IQuery
       (query [this]
              [{:proplist (om/get-query PropertyList)} {:main (om/get-query MainArea)}])
       Object
       (render [this]
               (let [{:keys [proplist main]} (om/props this)]
                    (dom/div #js {:style #js {:fontFamily "Varela Round"}}
                             (property-list proplist)
                             (main-area main)))))

(def app (om/factory App))

(def reconciler
     (om/reconciler {:state  conn
                      :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
              App (gdom/getElement "app"))


  
