(ns orly-example.content
    (:require [om.dom :as dom]))

(def initial-data
     [{:db/id -1
       :rect/content :main
       :rect/relwidth 8
       :rect/relheight 6}
      {:db/id -2
       :rect/content :owl
       :rect/relwidth 6
       :rect/relheight 6}
      {:db/id -3
       :rect/content :resize
       :rect/relwidth 4
       :rect/relheight 4}
      {:db/id -4
       :rect/content :yarly
       :rect/relwidth 4
       :rect/relheight 3}
      {:db/id -5
       :rect/content :warning
       :rect/relwidth 4
       :rect/relheight 3}
      {:db/id -6
       :rect/content :ha-owl
       :rect/relwidth 3
       :rect/relheight 2}
      {:db/id -7
       :rect/content :credits
       :rect/relwidth 3
       :rect/relheight 1}
      {:db/id -8
       :rect/content :om
       :rect/relwidth 4
       :rect/relheight 4}
      {:db/id -9
       :app/grow false}])

(defn light [text]
      (dom/span #js {:style #js {:color "silver"}}
                text))

(def tile-content
     {:main {:color "white"
             :html (dom/div #js {:style #js {:paddingLeft "1em"
                                             :paddingRight "1em"
                                             :height "100%"
                                             :overflow "auto"}}
                            (dom/h2 nil 
                                    (dom/i nil "ORLY")
                                    (light ", the ")
                                    (dom/br nil)
                                    "O"
                                    (light "m ")
                                    "R"
                                    (light "ectangle ")
                                    "L"
                                    (light "ayout librar")
                                    "Y")
                            "Built on Om Next, this library publishes a single component, named "
                            (dom/i nil "Orly")
                            ", which optimally packs arbitrary child components inside of itself- You simply pass it these components and it handles the layout. It implements a reasonably fast and reasonably efficient rectangle packing algorithm, using desired relative widths and heights, which you also supply, in the standard Om Next manner (see the "
                            (dom/a #js {:href "https://github.com/drcode/orly"} "github page")
                            " for details on usage)"
                            (dom/br nil)
                            (dom/br nil)
                            "To see how the Orly layout enigine works, press the +/- buttons on the left and see what happens! (Also, try out the checkbox below the buttons)"
                            (dom/br nil)
                            (dom/h3 nil "Benefits of the ORLY and Om Next design:")
                            (dom/ol nil
                                    (dom/li nil
                                            "Instead of mucking with the raw DOM, all layout actions of the Orly component are performed as transactions against your app's immutable client state, using the flexible Om Next \"query selector\" mechanism. This means all layout activity is done in a tidy fashion and under your app's full control.")
                                    (dom/li nil
                                            "Child components (i.e. the tiles) are just plain React components, defined by your app. They are sent their correct coordinates as needed through the Om Next data propagation mechanism, again through \"query selectors\" but otherwise can contain completely arbitrary DOM content and styling.")))}
      :owl {:image "owl.png"}
      :resize {:color "#44D"
               :html  (dom/h3 #js {:style #js {:paddingLeft "1em"
                                               :paddingRight "1em"
                                               :color "white"}}
                            
                              "Resize your browser window to see Orly adapt the layout in real time!")}
      :warning {:color "#F22"
               :html  (dom/h3 #js {:style #js {:paddingLeft "1em"
                                               :paddingRight "1em"
                                               :color "white"
                                               :textAlign "center"}}
                            
                              "WARNING!"
                              (dom/br nil)
                              "Like Om Next, ORLY is still an experimental library!")}
      :ha-owl {:image "ha.png"}
      :yarly {:image "yarly.jpg"}
      :credits {:color "#555"
                :html (dom/div #js {:style #js {:paddingLeft "1em"
                                                :paddingRight "1em"
                                                :paddingTop "0.2em"
                                                :fontSize "0.9em"
                                                :color "white"
                                                :textAlign "right"}}
                               "Copyright 2015"
                               (dom/br nil)
                               "Conrad Barski")}
      :om {:color "#2B2"
           :html  (dom/div #js {:style #js {:paddingLeft "1em"
                                               :paddingRight "1em"
                                               :color "white"}}
                           (dom/h2 nil "Om Next...")
                           "...is a new Clojurescript client library built on React, borrowing ideas from Falcor, Datomic, and Relay. See " (dom/a #js {:href "http://livestream.com/intentmedia/events/4386134"} "this video") " for more info.")
           }})
