(set-env!
 :source-paths #{"src"}
 :resource-paths #{"html"}
 :dependencies '[[adzerk/boot-cljs "1.7.48-3" :scope "test"]
                 [org.clojure/tools.nrepl "0.2.10" :scope "test"]
                 [com.cemerick/piggieback "0.2.1"]
                 [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT" :scope "test"]
                 [adzerk/boot-reload "0.3.1" :scope "test"]
                 [pandeiro/boot-http "0.6.3" :scope "test"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.12.0"]])

(require
 '[adzerk.boot-cljs :refer [cljs]]
 '[adzerk.boot-reload :refer [reload]]
 '[pandeiro.boot-http :refer [serve]])

(require 'boot.repl)
(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.9.1"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)

(deftask dev []
  (set-env! :source-paths #{"src"})
  (comp (serve :dir "target/")
        (watch)
        (speak)
        (reload :on-jsload 'netzwaechterlein.client/main)
        (cljs :source-map true
              :optimizations :none)))

(deftask build []
  (set-env! :source-paths #{"src"})
  (comp (cljs :optimizations :advanced)))
