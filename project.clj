(defproject netzwaechterlein "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.12.0"]
                 [cljsjs/moment "2.9.0-3"]
                 [figwheel "0.3.9"]
                 [rum "0.3.0"]
                 [jarohen/chord "0.6.0"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :npm {:dependencies [[source-map-support "0.3.2"]
                       [every-moment "0.0.1"]
                       [net-ping "1.1.11"]
                       [express "4.13.3"]
                       [serve-static "1.10.0"]
                       [express-ws "1.0.0-rc.1"]]}
  :clean-targets ^{:protect false}
  ["resources/public/js/out"
   "resources/public/js/client.js"
   "target"]
  :resource-paths ["resources"]
  :target-path "target"
  :plugins [[lein-npm "0.6.1"]
            [lein-figwheel "0.3.9"]]

  :figwheel {:open-file-command "emacsclient" :nrepl-port 7888}

  :cljsbuild {:builds
              [{:id "backend.dev"
                :source-paths ["backend" "backend.dev"]
                :compiler
                {:output-to "target/backend.dev/server.js"
                 :output-dir "target/backend.dev"
                 :optimizations :none
                 :main netzwaechterlein.dev
                 :warnings {:single-segment-namespace false}
                 :target :nodejs}}
               {:id "backend"
                :source-paths ["backend"]
                :compiler
                {:output-to "target/backend/server.js"
                 :output-dir "target/backend"
                 :optimizations :none
                 :main netzwaechterlein.server
                 :warnings {:single-segment-namespace false}
                 :target :nodejs}}
               {:id "frontend"
                :source-paths ["frontend"]
                :figwheel true
                :compiler
                {:asset-path "js/out"
                 :output-to "resources/public/js/client.js"
                 :output-dir "resources/public/js/out"
                 :on-jsload "netzwaechterlein.client/on-js-reload"
                 :main netzwaechterlein.client
                 :warnings {:single-segment-namespace false}
                 :optimizations :none}}]}

  :profiles {:dev {:source-paths ["frontend" "backend" "lambda"]}})
