(ns netzwaechterlein.websocket-test
  (:require [netzwaechterlein.websocket :refer [publish-websocket]]
            [netzwaechterlein.core :refer [setup-netwatch]]
            [netzwaechterlein.db :refer [init-db]]
            [netzwaechterlein.server :refer [WebSocketServer Database]]
            [cljs.test :as t :refer-macros [deftest async is]]
            [cljs.core.async :refer [<! chan timeout pipe put!]]
            [datascript.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce WebSocket (js/require "ws"))

(deftest test-publish-ws
  (let [pull-chan (chan)
        ws-result-chan (chan)
        db (Database. ":memory:")
        test-sensor {:type :hello :status :ok :timestamp (.getTime (js/Date.)) :message nil}
        ws-server (WebSocketServer. #js {:port 8082})
        ws (WebSocket. "ws://localhost:8082")
        result-or-timeout [ws-result-chan (timeout 100)]]
    (init-db db)
    (.on ws "message" (fn [data _] (put! ws-result-chan (cljs.reader/read-string data))))
    (setup-netwatch
     {:pull-chan pull-chan
      :sensor-fns [#(put! %1 test-sensor)]
      :publish-fns [(partial publish-websocket db ws-server)]})
    (async done
      (go
        (>! pull-chan :pull-sensors)
        (is (= (d/empty-db) (first (alts! result-or-timeout))))
        (is (= test-sensor (first (alts! result-or-timeout))))
        (>! pull-chan :pull-sensors)
        (is (= test-sensor (first (alts! result-or-timeout))))
        (done)
        (.close ws)
        (.close ws-server)
        (.close db)))))
