(ns ^:figwheel-always
  test.netzwaechterlein.server-test
  (:require [cljs.test :as t :refer-macros [deftest testing is async]]
            [cljs.core.async :as a :refer [<! >! put! chan mult]]
            [netzwaechterlein.server :refer [add-sensor]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(deftest add-sensor-test
  (let [sensor-chan (chan)
        pull-chan (chan)
        pull-mult (mult pull-chan)
        handler (fn [result sensor-chan] (put! sensor-chan result))]
    (async
     done
     (add-sensor pull-mult sensor-chan (partial handler :sensor-result))
     (go
       (>! pull-chan :kick-off)
       (is (= :sensor-result (<! sensor-chan)))
       (done)))))

(t/run-tests)
