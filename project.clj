(defproject netzwaechterlein "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.12.2"]
                 [figwheel "0.3.9"]
                 [rum "0.3.0"]
                 [jarohen/chord "0.6.0"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :npm {:dependencies [[source-map-support "0.3.2"]
                       [net-ping "1.1.11"]
                       [express "4.13.3"]
                       [serve-static "1.10.0"]
                       [ws "0.8.0"]]}
  :clean-targets ^{:protect false}
  ["resources/public/js/out"
   "resources/public/js/client.js"
   "target"]
  :resource-paths ["resources"]
  :target-path "target"
  :plugins [[lein-npm "0.6.1"]
            [lein-figwheel "0.3.9"]
            [lein-cljsbuild "1.1.0"]]

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
                 :optimizations :simple
                 :main netzwaechterlein.server
                 :warnings {:single-segment-namespace false}
                 :target :nodejs
                 :preamble ["preamble.js"]}}
               {:id "frontend.dev"
                :source-paths ["frontend"]
                :compiler
                {:asset-path "js/out"
                 :output-to "resources/public/js/client.js"
                 :output-dir "resources/public/js/out"
                 :main netzwaechterlein.client
                 :warnings {:single-segment-namespace false}
                 :optimizations :none}}
               {:id "frontend"
                :source-paths ["frontend"]
                :compiler
                {:output-to "resources/public/js/client.js"
                 :main netzwaechterlein.client
                 :warnings {:single-segment-namespace false}
                 :optimizations :advanced}}]}

  :profiles {:dev {:source-paths ["frontend" "backend"]}})
