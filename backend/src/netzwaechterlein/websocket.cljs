(ns netzwaechterlein.websocket
  (:require [cljs.core.async :as async :refer [<!]]
            [netzwaechterlein.db :refer [dump-db]]
            [datascript.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn data->client [db sensor-chan ws]
  (go
    (when-let [db-dump (<! (dump-db db))]
      (.send ws (pr-str (d/db-with (d/empty-db) db-dump))))
    (loop []
      (when-let [msg (<! sensor-chan)]
        (.send ws (pr-str msg)
               (fn [e] (when e (async/close! sensor-chan))))
        (recur)))))

(defn publish-websocket [db ws-server sensor-chan]
  (. ws-server (on "connection" (partial data->client db sensor-chan))))
