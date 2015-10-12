(ns netzwaechterlein.core
  (:require [cljs.core.async :as a :refer [<! >! chan timeout mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn create-sensor [pull-sensor-mult f]
  (let [pull-sensor-chan (chan)
        _ (tap pull-sensor-mult pull-sensor-chan)
        sensor-chan (chan)]
    (go-loop []
      (<! pull-sensor-chan)
      (f sensor-chan)
      (recur))
    sensor-chan))

(defn create-publisher [f sensor-mult]
  (let [sensor-chan (chan)
        _ (tap sensor-mult sensor-chan)]
    (f sensor-chan)))

(defn every [ms]
  (let [pull-chan (chan)]
    (go-loop []
      (<! (timeout ms))
      (>! pull-chan :pull-sensors)
      (recur))
    pull-chan))

(defn setup-netwatch [{:keys [pull-chan sensor-fns publish-fns]}]
  (let [pull-sensor-mult (mult pull-chan)
        sensor (partial create-sensor pull-sensor-mult)
        sensor-mult (mult (a/merge (map sensor sensor-fns)))]
    (doseq [publish-fn publish-fns]
      (create-publisher publish-fn sensor-mult))))
