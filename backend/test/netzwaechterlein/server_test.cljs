(ns netzwaechterlein.server-test
  (:require [cljs.test :as t :refer-macros [deftest testing is async use-fixtures]]
            [cljs.core.async :as a :refer [<! >! put! chan mult]]
            [netzwaechterlein.server :refer [create-sensor ping-host dns-lookup setup-netwatch publish-websocket WebSocketServer]]
            [datascript.core :as d]
            [cljs.reader :refer [read-string]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce WebSocket (js/require "ws"))
(defonce ws-server (WebSocketServer. #js {:port 8082}))

(deftest create-sensor-test
  (let [sensor-pull-chan (chan)
        sensor-pull-mult (mult sensor-pull-chan)
        sensor-chan (create-sensor sensor-pull-mult #(put! %1 :sensor-result))]
    (async done
      (go (>! sensor-pull-chan :kick-off)
          (is (= :sensor-result (<! sensor-chan)))
          (done)))))

(defn test-sensor-chan [] (chan 1 (map #(dissoc %1 :timestamp))))

(defn ok [type] {:type type :status :ok})

(defn not-ok [type message]
  {:type type, :status :error, :message message})

(defn test-sensor [sensor-fn address expected]
  (let [sensor-chan (test-sensor-chan)]
    (async done
      (go
        (sensor-fn address sensor-chan)
        (is (= expected (<! sensor-chan)))
        (done)))))

(deftest ping-ok
  (test-sensor ping-host
               "64.233.166.105"
               (ok :ping)))

(deftest ping-not-ok
  (test-sensor ping-host
               "10.10.10.10.10"
               (not-ok :ping "Error: Invalid IP address '10.10.10.10.10'")))

(deftest lookup-ok
  (test-sensor dns-lookup
               "www.google.at"
               (ok :dns)))

(deftest lookup-not-ok
  (test-sensor dns-lookup
               "www.www.www"
               (not-ok :dns "Error: queryA ENOTFOUND")))

(deftest publish-websocket-test
  (let [pull-chan (chan)
        ws-chan (chan)]
    (setup-netwatch
     {:pull-chan pull-chan
      :sensor-fns [#(put! %1 :sensor-result)]
      :publish-fns [(partial publish-websocket ws-server)]})
    (async done
      (go
        (let [ws (WebSocket. "ws://localhost:8082")]
          (>! pull-chan :kick-off)
          (. ws (on "message" (fn [data flags]
                                (put! ws-chan (read-string data)))))
          (is (= (d/empty-db) (<! ws-chan)))
          (is (= :sensor-result (<! ws-chan)))
          (done))))))

(use-fixtures :once {:after #(.close ws-server)})
