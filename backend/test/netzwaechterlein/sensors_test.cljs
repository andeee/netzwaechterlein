(ns netzwaechterlein.sensors-test
  (:require [cljs.test :as t :refer-macros [deftest async is]]
            [cljs.core.async :refer [<! chan]]
            [netzwaechterlein.sensors :refer [ping-host dns-lookup]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn ok [type] {:type type :status :ok})

(defn not-ok [type message]
  {:type type, :status :error, :message message})

(defn test-sensor-chan [] (chan 1 (map #(dissoc %1 :timestamp))))

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
