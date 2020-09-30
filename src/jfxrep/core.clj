(ns jfxrep.core
  (:require
   [clojure.tools.namespace.repl :as tn :refer [refresh]]
   [cljfx
    [api :as fx]]
   [cljfx.css :as css]))
(def app-state (atom {:list-of-maps [{:label "dummy value one" :value "foo"}
                                     {:label "dummy value two" :value "bar"}
                                     {:label "dummy value three" :value "foo"}
                                     ]
                      :index 0}))

(defn select-newest!
  "updates the selected map to the last map in the list of maps"
  []
  (swap! app-state assoc :index (-> @app-state :list-of-maps count dec)))

(defn add-map!
  "adds a new map to the end of the list of maps"
  []
  (swap! app-state update-in [:list-of-maps] conj {:label "dummy value x" :value "bup"})
  (select-newest!))

(defn rm-map!
  "removes a map from the end of the list of maps"
  []
  (swap! app-state update-in [:list-of-maps] butlast)
  (select-newest!))

;;

(defn dropdown-one
  [{:keys [fx/context]}]
  (let [app-state (fx/sub-val context get :app-state)
        map-list (:list-of-maps app-state)
        selected (:index app-state)
        selected-label (:label (.get map-list selected))]
    
    (println "dropdown one, received state:" app-state)
    (println "dropdown one, selected label:" selected-label)

    {:fx/type :combo-box
     :value selected-label
     :items (mapv :label map-list)}))

(defn dropdown-two
  [{:keys [fx/context]}]
  (let [app-state (fx/sub-val context get :app-state)
        map-list (:list-of-maps app-state)
        selected (.get map-list (:index app-state))
        ]
    
    (println "dropdown two, received state:" app-state)
    (println "dropdown two, selected value:" selected)

    {:fx/type :combo-box
     :value selected
     :button-cell (fn [row] {:text (:label row)})
     :cell-factory {:fx/cell-type :list-cell
                    :describe (fn [row] {:text (:label row)})}
     :items map-list}))

(defn dropdown-three
  [{:keys [fx/context]}]
  (let [map-list (fx/sub-val context get-in [:app-state :list-of-maps])
        index (fx/sub-val context get-in [:app-state :index])
        selected (.get map-list index)
        ]
    
    (println "dropdown three, received map-list:" map-list)
    (println "dropdown three, received index:" index)
    (println "dropdown three, selected value:" selected)

    {:fx/type :combo-box
     :value (:label selected)
     :items (mapv :label map-list)}))

(defn menu-bar
  [{:keys [fx/context]}]
  {:fx/type :h-box
   :padding 10
   :spacing 10
   :children [{:fx/type :button :text "add" :on-action (fn [_] (add-map!))}
              {:fx/type :button :text "rm" :on-action (fn [_] (rm-map!))}
              {:fx/type dropdown-one}
              {:fx/type dropdown-two}
              {:fx/type dropdown-three}
              ]})

(defn gui
  [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type menu-bar}]}}})

(defn -main
  [& args]
  (Thread/sleep 500)
  (println "--------------- init")
  (let [gui-state (atom (fx/create-context {:app-state @app-state}))
        update-gui-state (fn [_ _ _ new-state]
                           (println "--------------- app state change, updating gui state")
                           (swap! gui-state fx/swap-context assoc :app-state new-state))
        _ (add-watch app-state :app update-gui-state)

        renderer (fx/create-renderer
                  :middleware (comp
                               fx/wrap-context-desc
                               (fx/wrap-map-desc (fn [_] {:fx/type gui})))
                  :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                                      (fx/fn->lifecycle-with-context %))})
        ]
    (fx/mount-renderer gui-state renderer))
  nil)
