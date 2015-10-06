(ns netzwaechterlein.dev
  (:require
   [figwheel.client]
   [netzwaechterlein.server]))

(defn -main [& _]
  (netzwaechterlein.server/-main))

(set! *main-cli-fn* -main)

(figwheel.client/start { })
