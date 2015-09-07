(ns netzwaechterlein.core
  (:require [cljs.core.async :as async :refer [<!]]
            [cljsjs.moment]
            [datascript :as d])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def every (js/require "every-moment"))
(def ping (js/require "net-ping"))
(def dns (js/require "dns"))

(enable-console-print!)

(def nw-ch (async/chan))

(defn send-watch [error m]
  (if-not error
    (async/put! nw-ch (merge m {:status :ok}))
    (async/put! nw-ch (merge m {:status :error :messge (str error)}))))

(defn ping-host [address]
  (let [session (.createSession ping)]
    (.pingHost
     session
     address
     (fn [error _]
       (send-watch error {:type :ping})
       (.close session)))))

(defn dns-lookup [address]
  (.resolve
   dns
   address
   (fn [error _ _] (send-watch error {:type :dns}))))

(defn netwatch []
  (ping-host "64.233.166.105")
  (dns-lookup "www.google.com"))

(go-loop []
  (let [msg (<! nw-ch)]
    (println msg)
    (recur)))

(defonce timer
  (every 20 (name :second) netwatch))
