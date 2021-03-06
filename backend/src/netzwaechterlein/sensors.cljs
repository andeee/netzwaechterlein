(ns netzwaechterlein.sensors
  (:require [cljs.core.async :refer [put!]]))

(defonce request (js/require "request"))
(defonce dns (js/require "dns"))

(defn send-watch [error type sensor-chan]
  (let [timestamp {:timestamp (.getTime (js/Date.))}
        merge-watch (partial merge {:type type} timestamp)]
    (if-not error
      (put! sensor-chan (merge-watch {:status :ok}))
      (put! sensor-chan (merge-watch {:status :error :message (str error)})))))

(defn ping-host [address sensor-chan]
  (.get request
        #js {:method "HEAD" :uri (str "http://" address)}
        (fn [error _ _]
          (send-watch error :ping sensor-chan))))

(defn dns-lookup [address sensor-chan]
  (.resolve
   dns
   address
   (fn [error _ _] (send-watch error :dns sensor-chan))))
