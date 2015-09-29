(defproject netzwaechterlein "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.13.1"]
                 [figwheel "0.4.0"]
                 [rum "0.4.1"]
                 [jarohen/chord "0.6.0"]
                 [doo "0.1.5-SNAPSHOT"]]
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
            [lein-figwheel "0.4.0"]
            [lein-cljsbuild "1.1.0"]
            [lein-doo "0.1.5-SNAPSHOT"]]

  :figwheel {:open-file-command "emacsclient" :nrepl-port 7888}

  :cljsbuild {:builds
              [{:id "backend.dev"
                :source-paths ["backend/src" "backend/dev" "backend/test"]
                :compiler
                {:output-to "target/backend.dev/server.js"
                 :output-dir "target/backend.dev"
                 :optimizations :none
                 :main "netzwaechterlein.dev"
                 :warnings {:single-segment-namespace false}
                 :target :nodejs}}
               {:id "backend.test"
                :source-paths ["backend/src" "backend/test"]
                :compiler
                {:output-to "target/backend.test/server-test.js"
                 :output-dir "target/backend.test"
                 :optimizations :none
                 :main "netzwaechterlein.runner"
                 :warnings {:single-segment-namespace false}
                 :target :nodejs}}
               {:id "backend"
                :source-paths ["backend/src"]
                :compiler
                {:output-to "target/backend/server.js"
                 :output-dir "target/backend"
                 :optimizations :simple
                 :main netzwaechterlein.server
                 :warnings {:single-segment-namespace false}
                 :target :nodejs
                 :preamble ["preamble.js"]}}
               {:id "frontend.dev"
                :source-paths ["frontend/src"]
                :compiler
                {:asset-path "js/out"
                 :output-to "resources/public/js/client.js"
                 :output-dir "resources/public/js/out"
                 :main netzwaechterlein.client
                 :warnings {:single-segment-namespace false}
                 :optimizations :none}}
               {:id "frontend"
                :source-paths ["frontend/src"]
                :compiler
                {:output-to "resources/public/js/client.js"
                 :main netzwaechterlein.client
                 :warnings {:single-segment-namespace false}
                 :optimizations :advanced}}]}
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
