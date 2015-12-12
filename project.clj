(defproject netzwaechterlein "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.13.3"]
                 [figwheel "0.5.0-2"]
                 [rum "0.6.0"]
                 [jarohen/chord "0.6.0"]
                 [doo "0.1.5"]
                 [cljsjs/moment "2.10.6-0"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :npm {:dependencies [[source-map-support "0.3.2"]
                       [net-ping "1.1.11"]
                       [express "4.13.3"]
                       [serve-static "1.10.0"]
                       [ws "0.8.0"]
                       [sqlite3 "3.1.0"]]}
  :clean-targets ^{:protect false}
  ["resources/public/js/out"
   "resources/public/js/client.js"
   "target"]
  :resource-paths ["resources"]
  :target-path "target"
  :plugins [[lein-npm "0.6.1"]
            [lein-figwheel "0.5.0-2"]
            [lein-cljsbuild "1.1.0"]
            [lein-doo "0.1.5"]]

  :figwheel {:open-file-command "emacsclient"
             :nrepl-port 7888
             :nrepl-middleware ["cider.nrepl/cider-middleware"
                                "refactor-nrepl.middleware/wrap-refactor"
                                "cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["resources/public/css"]}

  :cljsbuild {:builds
              [{:id "backend.dev"
                :source-paths ["backend/src" "backend/dev" "backend/test"]
                :figwheel true
                :compiler
                {:output-to "target/backend.dev/server.js"
                 :output-dir "target/backend.dev"
                 :optimizations :none
                 :main "netzwaechterlein.dev"
                 :target :nodejs}}
               {:id "backend.test"
                :source-paths ["backend/src" "backend/test"]
                :compiler
                {:output-to "target/backend.test/server-test.js"
                 :output-dir "target/backend.test"
                 :optimizations :none
                 :main "netzwaechterlein.doo.runner"
                 :target :nodejs}}
               {:id "backend"
                :source-paths ["backend/src"]
                :compiler
                {:output-to "target/backend/server.js"
                 :output-dir "target/backend"
                 :optimizations :simple
                 :main "netzwaechterlein.server"
                 :target :nodejs
                 :preamble ["preamble.js"]}}
               {:id "frontend.dev"
                :source-paths ["frontend/src"]
                :figwheel true
                :compiler
                {:asset-path "js/out"
                 :output-to "resources/public/js/client.js"
                 :output-dir "resources/public/js/out"
                 :main "netzwaechterlein.client"
                 :optimizations :none}}
               {:id "frontend"
                :source-paths ["frontend/src"]
                :compiler
                {:output-to "resources/public/js/client.js"
                 :main "netzwaechterlein.client"
                 :optimizations :whitespace}}]}
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [cider/cider-nrepl "0.10.0-SNAPSHOT"]
                                  [refactor-nrepl "2.0.0-SNAPSHOT"]]}})
