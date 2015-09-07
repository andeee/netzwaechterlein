(defproject netzwaechterlein "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.11.6"]
                 [cljsjs/moment "2.9.0-3"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-npm "0.6.1"]]
  :npm {:dependencies [[source-map-support "0.3.2"]
                       [every-moment "0.0.1"]
                       [net-ping "1.1.14"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release"]
  :target-path "target"
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
