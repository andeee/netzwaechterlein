(defproject netzwaechterlein "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.12.0"]
                 [cljsjs/moment "2.9.0-3"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :npm {:dependencies [[source-map-support "0.3.2"]
                       [every-moment "0.0.1"]
                       [net-ping "1.1.11"]
                       [serve-static "1.10.0"]
                       [express "4.13.3"]]}
  :clean-targets ^{:protect false}
  ["resources/public/js/out"
   "resources/public/js/client.js"
   "target"]
  :resource-paths ["resources"]
  :target-path "target"
  :plugins [[lein-npm "0.6.1"]
            [lein-cljsbuild "1.1.0"]]

  :figwheel {:open-file-command "emacsclient"}

  :cljsbuild {:builds
              [{:id "backend"
                :source-paths ["backend"]
                :compiler
                {:output-to "target/backend/server.js"
                 :output-dir "target/backend"
                 :optimizations :none
                 :main "netzwaechterlein.server"
                 :target :nodejs}}
               {:id "frontend"
                :source-paths ["frontend"]
                :figwheel true
                :compiler
                {:asset-path "js/out"
                 :output-to "resources/public/js/client.js"
                 :output-dir "resources/public/js/out"
                 :optimizations :advanced}}]}

  :profiles {:dev {:source-paths ["frontend" "backend" "lambda"]
                   :dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [figwheel "0.3.9"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
