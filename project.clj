(defproject jfxrep "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 ;; reload code with `(clojure.tools.namespace.repl/refresh)`
                 [org.clojure/tools.namespace "1.0.0"] 
                 [cljfx "1.7.8"]
                 [cljfx/css "1.1.0"]]

  :main jfxrep.combo-box-blank)
