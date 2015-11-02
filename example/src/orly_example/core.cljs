(ns orly-example.core
    (:require [goog.dom :as gdom]
              [om.next :as om :refer-macros [defui]]
              [om.dom :as dom]
              [datascript.core :as d]
              [orly.core :as o]
              [orly-example.content :as co]))

(def conn (d/create-conn {}))

(d/transact! conn co/initial-data)

(enable-console-print!)

(defmulti read om/dispatch)

(defmethod read :rects [{:keys [state selector] :as env} _ _]
           {:value (d/q '[:find [(pull ?e ?selector) ...]
                          :in $ ?selector 
                          :where [?e :rect/content ?content]]
                        (d/db state)
                        selector)})

(defmethod read :app [{:keys [state selector] :as env} _ _]
           {:value (first (d/q '[:find [(pull ?e ?selector) ...]
                            :in $ ?selector 
                            :where [?e :app/grow ?grow]]
                          (d/db state)
                          selector))})

(defmethod read :proplist [{:keys [selector parser] :as env} _ _]
           {:value (parser env selector)})

(defmethod read :layout [{:keys [selector parser] :as env} _ _]
           {:value (parser env selector)})

(defmethod read :main [{:keys [state selector parser] :as env} _ _]
           {:value (parser env selector)})

(defmulti mutate om/dispatch)

(defmethod mutate 'app/update-rect
           [{:keys [state]} _ rect]
           {:value  []
            :action (fn []
                        (d/transact! state [rect]))})

(defmethod mutate 'app/update-app
           [{:keys [state]} _ app]
           {:value  []
            :action (fn []
                        (d/transact! state [app]))})

(defui Property
       static om/IQuery
       (query [this]
              [:db/id :rect/relwidth :rect/relheight])
       Object
       (render [this]
               (let [entity   (om/props this)
                     {:keys [on-select-change selected]} (om/get-computed entity)
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
                                                                                                     {:main [{:layout [{:rects [:rect/relwidth :rect/relheight]}]}] }]))
                                                                   }
                                                              (if positive?
                                                                       "+"
                                                                       "-")))]
                                       (dom/div #js {:style #js {:padding "0.2em"
                                                                 :padding-right "1.8em"
                                                                 :backgroundColor "#DDD"}}
                                                text
                                                " "
                                                (twiddle false)
                                                " "
                                                (key entity)
                                                " "
                                                (twiddle true))))]
                    
                    (dom/div #js {:style #js {:textAlign "right"
                                              :opacity (if selected
                                                           "1.0"
                                                           "0.7")}
                                  :onMouseEnter (fn []
                                                    (on-select-change (:db/id entity))
                                                    #_(om/transact! this `[(app/update-app ~(assoc entity :selected-rect (:db/id entity)))]))
                                  :onMouseLeave (fn []
                                                    (on-select-change false)
                                                    #_(om/transact! this `[(app/update-app ~(assoc entity :selected-rect false))]))}
                             (prop-row :rect/relwidth "Width")
                             (prop-row :rect/relheight "Height")
                             (dom/div #js {:style #js {:height          "0.2em"
                                                       :backgroundColor "grey"}})))))

(def property (om/factory Property {:keyfn :db/id}))

(defui PropertyList
       static om/IQuery
       (query [this]
              [{:rects (om/get-query Property)} {:app [:db/id :app/selected-rect :app/grow]}])
       Object
       (render [this]
               (let [{:keys [rects app]} (om/props this)
                     {:keys [:app/selected-rect :app/grow]} app]
                    (dom/div #js {:style #js {:position        "absolute"
                                              :top             0
                                              :left            0
                                              :width           "10em"
                                              :height          "100%"
                                              :backgroundColor "grey"
                                              :textAlign       "center"}}
                             (dom/div #js {:style #js {:backgroundColor "grey"
                                                       :padding         "0.5em"
                                                       :color           "white"}}
                                      "Relative Dimensions")
                             (for [r rects]
                                  (property (om/computed r
                                                         {:selected (= (:db/id r) selected-rect)
                                                          :on-select-change (fn [id]
                                                                                (om/transact! this `[(app/update-app ~(assoc app :app/selected-rect id))
                                                                                                     {:main [:app]}]))})))
                             (dom/label #js {:style #js {:paddingLeft "1em"
                                                         :paddingRight "1em"}}
                                        (dom/input #js {:type "checkbox"
                                                                            :id "grow_checkbox"
                                                                            :style #js {:margin-top "1em"}
                                                                            :checked grow
                                                                            :onChange (fn [ev]
                                                                                          (om/transact! this `[(app/update-app ~(assoc app :app/grow (.-checked (.-target ev))))
                                                                                                               {:main [:app]}]))})
                                        "Expand tiles to fit space")
))))

(def property-list (om/factory PropertyList))

(defui Child
       static om/IQuery
       (query [this]
              [:db/id :rect/content :rect/width :rect/height :rect/left :rect/top])
       Object
       (render [this]
               (let [entity (om/props this)
                     {:keys [:db/id :rect/content :rect/width :rect/height :rect/top :rect/left]} entity
                     {:keys [selected on-select-change]} (om/get-computed entity)
                     {:keys [color html image]} (co/tile-content content)]
                    (dom/div #js {:style #js {:backgroundColor color
                                              :backgroundImage (str "url(images/" image ")")
                                              :backgroundSize  "100% 100%"
                                              :overflow        "hidden"
                                              :position        "absolute"
                                              :outlineWidth    "0.2em"
                                              :outlineColor    "white"
                                              :outlineStyle    (if selected
                                                                   "solid"
                                                                   "none")
                                              :zIndex          (if selected
                                                                   100
                                                                   0)
                                              :width           (or width 0)
                                              :height          (or height 0)
                                              :left            (or left 0)
                                              :top             (or top 0)}
                                  :onMouseEnter (fn []
                                                    (on-select-change (:db/id entity)))
                                  :onMouseLeave (fn []
                                                    (on-select-change false))}
                             (or html color)))))

(def child (om/factory Child {:keyfn :db/id}))

(def orly (om/factory o/Orly))

(defui MainArea
       static om/IQuery
       (query [this]
              [{:layout (om/get-query o/Orly)} {:rects (om/get-query Child)} {:app [:db/id :app/selected-rect :app/grow]}])
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
                                                  :backgroundColor "#111"}}
                                 (let [{:keys [rects layout app]} (om/props this)
                                       {:keys [:app/selected-rect :app/grow]} app]
                                      (orly (om/computed layout {:grow grow})
                                            (for [item rects]
                                                 (child (om/computed item
                                                                     {:selected         (= (:db/id item) selected-rect)
                                                                      :on-select-change (fn [id]
                                                                                            (om/transact! this `[(app/update-app ~(assoc app :app/selected-rect id))
                                                                                                                 {:proplist [:rects :app]}]))})))))))))

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


  
