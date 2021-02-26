(ns jfxrep.new-tabs
  (:require
   [clojure.tools.namespace.repl :as tn :refer [refresh]]
   [cljfx
    [api :as fx]])
  (:import
   [javafx.stage Window]))

(def app-state (atom {:list-of-tabs []}))

(defn new-tab!
  [label body closeable]
  (swap! app-state update-in [:list-of-tabs] conj {:fx/type :tab
                                                   :id (str (java.util.UUID/randomUUID))
                                                   :text label
                                                   :closable closeable
                                                   :content {:fx/type :label
                                                             :text body}})
  nil)

(defn tabber
  [{:keys [fx/context]}]
  (let [tab-list (:list-of-tabs (fx/sub-val context get :app-state))]
    {:fx/type :tab-pane
     :id "tabber"
     :tabs tab-list}))

(defn get-tab-pane
  []
  (-> (Window/getWindows) first .getScene .getRoot (.lookupAll "#tabber") first))

(defn menu-bar
  [_]
  {:fx/type :h-box
   :children [{:fx/type :button
               :text "cljfx new tab"
               :on-action (fn [_]
                            (let [num (-> @app-state :list-of-tabs count inc)]
                              (new-tab! (str "tab" num) (str "body " num) true)))}

              {:fx/type :button
               :text "interop new tab 2"
               :on-action (fn [_]
                            (let [num (-> (get-tab-pane) .getTabs count inc)
                                  tab (doto (javafx.scene.control.Tab.)
                                        (.setText (str "tab" num)))
                                  ]
                              (-> (get-tab-pane) .getTabs (.add tab))))
               }]})

(defn gui
  [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type menu-bar}
                             {:fx/type tabber}]}}})

(defn -main
  [& args]
  (Thread/sleep 500)
  (let [gui-state (atom (fx/create-context {:app-state @app-state}))
        update-gui-state (fn [_ _ _ new-state]
                           (swap! gui-state fx/swap-context assoc :app-state new-state))
        _ (add-watch app-state :app update-gui-state)

        renderer (fx/create-renderer
                  :middleware (comp
                               fx/wrap-context-desc
                               (fx/wrap-map-desc (fn [_] {:fx/type gui})))
                  :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                                      (fx/fn->lifecycle-with-context %))})]
    (fx/mount-renderer gui-state renderer)
    renderer))
