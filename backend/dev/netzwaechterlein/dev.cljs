(ns netzwaechterlein.dev
  (:require [figwheel.client]
            [netzwaechterlein.server]))

(defn -main [& _]
  (figwheel.client/start {:id "backend.dev"})
  (netzwaechterlein.server/-main))

(set! *main-cli-fn* -main)