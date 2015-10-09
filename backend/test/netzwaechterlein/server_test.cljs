(ns netzwaechterlein.server-test
  (:require [cljs.test :as t :refer-macros [deftest testing is async use-fixtures]]
            [cljs.core.async :as a :refer [<! >! put! chan mult alts! timeout close! pipe]]
            [netzwaechterlein.server :refer [create-sensor ping-host dns-lookup setup-netwatch publish-db Database init-db sql->clj]]
            [datascript.core :as d]
            [cljs.reader :refer [read-string]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


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

(deftest test-setup-netwatch
  (let [pull-chan (chan)
        result-chan (chan)
        result (atom nil)]
    (setup-netwatch
     {:pull-chan pull-chan
      :sensor-fns [#(put! %1 :sensor-result)]
      :publish-fns [#(go (pipe %1 result-chan))]})
    (async done
      (go
        (>! pull-chan :kick-off)
        (is (= :sensor-result (first (alts! [result-chan (timeout 100)]))))
        (>! pull-chan :kick-off)
        (is (= :sensor-result (first (alts! [result-chan (timeout 100)]))))
        (done)))))

(deftest test-publish-db
  (let [sensor-chan (chan)
        db-result-chan (chan 1 sql->clj)
        db (Database. ":memory:")
        test-sensor {:type :hello :status :ok :timestamp (.getTime (js/Date.)) :message nil}]
    (publish-db db sensor-chan)
    (async done
      (go
        (>! sensor-chan test-sensor)
        (<! (timeout 10))
        (.get db "SELECT * FROM netwatch"
              (fn [err row]
                (if (or err (nil? row))
                  (println err)
                  (put! db-result-chan row))))
        (is (= test-sensor (first (alts! [db-result-chan (timeout 100)]))))
        (done)))))
