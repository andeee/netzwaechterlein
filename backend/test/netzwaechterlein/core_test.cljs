(ns netzwaechterlein.core-test
  (:require [cljs.test :as t :refer-macros [deftest async is]]
            [netzwaechterlein.core :refer [every create-sensor setup-netwatch]]
            [cljs.core.async :as a :refer [<! >! put! chan mult pipe alts! timeout close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest create-sensor-test
  (let [sensor-pull-chan (chan)
        sensor-pull-mult (mult sensor-pull-chan)
        sensor-chan (create-sensor sensor-pull-mult #(put! %1 :sensor-result))]
    (async done
      (go (>! sensor-pull-chan :kick-off)
          (is (= :sensor-result (<! sensor-chan)))
          (done)))))

(deftest test-setup-netwatch
  (let [pull-chan (chan)
        result-chan (chan)]
    (setup-netwatch
     {:pull-chan pull-chan
      :sensor-fns [#(put! %1 :sensor-result)]
      :publish-fns [#(go (pipe %1 result-chan))]})
    (async done
      (go
        (>! pull-chan :pull-sensors)
        (is (= :sensor-result (first (alts! [result-chan (timeout 100)]))))
        (>! pull-chan :pull-sensors)
        (is (= :sensor-result (first (alts! [result-chan (timeout 100)]))))
        (>! pull-chan :pull-sensors)
        (is (= :sensor-result (first (alts! [result-chan (timeout 100)]))))
        (done)))))

(defn get-result [ch]
  (first (alts! [ch (timeout 100)])))

(deftest test-setup-netwatch-multi
  (let [pull-chan (chan)
        result-chan-1 (chan)
        result-chan-2 (chan)
        result-chan (a/merge [result-chan-1 result-chan-2])]
    (setup-netwatch
     {:pull-chan pull-chan
      :sensor-fns [#(put! %1 :sensor-result-1)
                   #(put! %1 :sensor-result-2)]
      :publish-fns [#(go (pipe %1 result-chan-1))
                    #(go (pipe %1 result-chan-2))]})
    (async done
      (go
        (>! pull-chan :pull-sensors)
        (is (= :sensor-result-1 (first (alts! [result-chan (timeout 100)]))))
        (is (= :sensor-result-1 (first (alts! [result-chan (timeout 100)]))))
        (is (= :sensor-result-2 (first (alts! [result-chan (timeout 100)]))))
        (is (= :sensor-result-2 (first (alts! [result-chan (timeout 100)]))))
        (done)))))

(deftest test-every
  (let [pull-chan (every 100)]
    (async done
      (go
        (is (= :pull-sensors (<! pull-chan)))
        (is (= :pull-sensors (<! pull-chan)))
        (is (= :pull-sensors (<! pull-chan)))
        (done)
        (close! pull-chan)))))
