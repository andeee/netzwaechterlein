(ns netzwaechterlein.db-test
  (:require [cljs.test :as t :refer-macros [deftest async is]]
            [cljs.core.async :refer [<! >! put! chan alts! pipe timeout]]
            [netzwaechterlein.server :refer [Database]]
            [netzwaechterlein.core :refer [setup-netwatch]]
            [netzwaechterlein.db :refer [publish-db sql->clj dump-db]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn get-result-from-db [db db-result-chan]
  (pipe (dump-db db) db-result-chan))

(deftest test-publish-db
  (let [pull-chan (chan)
        result-chan (chan)
        db-result-chan (chan 1 (comp (map first) sql->clj))
        db (Database. ":memory:")
        test-sensor {:type :hello :status :ok :timestamp (.getTime (js/Date.))}]
    (setup-netwatch
     {:pull-chan pull-chan
      :sensor-fns [#(put! %1 test-sensor)]
      :publish-fns [#(publish-db db %1)
                    #(go (pipe %1 result-chan))]})
    (async done
      (go
        (>! pull-chan :pull-sensors)
        (is (= test-sensor (first (alts! [result-chan (timeout 100)]))))
        (get-result-from-db db db-result-chan)
        (is (= test-sensor (first (alts! [db-result-chan (timeout 100)]))))
        (done)))))
