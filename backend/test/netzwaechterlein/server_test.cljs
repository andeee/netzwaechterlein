(ns ^:figwheel-always
  netzwaechterlein.server-test
  (:require [cljs.test :as t :refer-macros [deftest testing is async]]
            [cljs.core.async :as a :refer [<! >! put! chan mult]]
            [netzwaechterlein.server :refer [create-sensor ping-host]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest create-sensor-test
  (let [pull-chan (chan)
        pull-mult (mult pull-chan)
        sensor-chan (create-sensor pull-mult #(put! %1 :sensor-result))]
    (async
     done
     (go (>! pull-chan :kick-off)
         (is (= :sensor-result (<! sensor-chan)))
         (done)))))

(testing "ping"
  (deftest ping-ok
    (let [sensor-chan (chan)]
      (async
       done
       (go
         (ping-host "64.233.166.105" sensor-chan)
         (let [sensor-result (<! sensor-chan)]
           (is (= {:type :ping :status :ok} sensor-result)))
         (done))))))
