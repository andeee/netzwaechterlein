(ns netzwaechterlein.server-test
  (:require [netzwaechterlein.websocket :refer [publish-websocket]]
            [netzwaechterlein.core :refer [setup-netwatch]]
            [netzwaechterlein.db :refer [sql->clj publish-db dump-db]]
            [netzwaechterlein.server :refer [WebSocketServer Database setup]]
            [cljs.test :as t :refer-macros [deftest async is]]
            [cljs.core.async :refer [<! chan timeout pipe put! onto-chan take!]]
            [datascript.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce WebSocket (js/require "ws"))

(defn get-results-from-db [db db-result-chan]
  (take! (dump-db db) #(onto-chan db-result-chan %1)))

(deftest test-end->end
  (let [pull-chan (chan)
        ws-result-chan (chan 1 (map #(if (map? %1) (dissoc %1 :timestamp) %1)))
        db-result-chan (chan 1 (comp sql->clj (map #(dissoc %1 :timestamp))))
        db (Database. ":memory:")
        ws-server (WebSocketServer. #js {:port 8083})
        ws (WebSocket. "ws://localhost:8083")
        ws-result-or-timeout [ws-result-chan (timeout 100)]
        db-result-or-timeout [db-result-chan (timeout 100)]]
    (.on ws "message" (fn [data _] (put! ws-result-chan (cljs.reader/read-string data))))
    (setup pull-chan db ws-server)
    (async done
      (go
        (>! pull-chan :pull-sensors)
        (is (= (d/empty-db) (first (alts! ws-result-or-timeout))))
        (is (= {:type :dns :status :ok} (first (alts! ws-result-or-timeout))))
        (is (= {:type :ping :status :ok} (first (alts! ws-result-or-timeout))))
        (get-results-from-db db db-result-chan)
        (is (= {:type :dns :status :ok} (first (alts! db-result-or-timeout))))
        (is (= {:type :ping :status :ok} (first (alts! db-result-or-timeout))))
        (done)
        (.close ws)
        (.close ws-server)
        (.close db)))))
