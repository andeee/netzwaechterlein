(ns netzwaechterlein.websocket
  (:require [cljs.core.async :as async :refer [<! chan mult tap]]
            [netzwaechterlein.db :refer [dump-db]]
            [datascript.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn data->client [db sensor-mult ws]
  (go
    (when-let [db-dump (<! (dump-db db))]
      (.send ws (pr-str (d/db-with (d/empty-db) db-dump))))
    (let [sensor-chan (chan)
          _ (tap sensor-mult sensor-chan)]
      (loop []
        (when-let [msg (<! sensor-chan)]
          (.send ws (pr-str msg)
                 (fn [e] (when e (async/close! sensor-chan))))
          (recur))))))

(defn publish-websocket [db ws-server sensor-chan]
  (. ws-server (on "connection" (partial data->client db (mult sensor-chan)))))
