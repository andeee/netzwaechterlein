(ns netzwaechterlein.server
  (:require [cljs.core.async :as async :refer [<!]]
            [datascript :as d])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def every (js/require "every-moment"))
(def ping (js/require "net-ping"))
(def dns (js/require "dns"))
(def http (js/require "http"))

(enable-console-print!)

(def nw-ch (async/chan))

(def conn (d/create-conn))

(defn send-watch [error m]
  (let [timestamp {:timestamp (js/Date.)}
        merge-watch (partial merge m timestamp)]
    (if-not error
      (async/put! nw-ch (merge-watch {:status :ok}))
      (async/put! nw-ch (merge-watch {:status :error :message (str error)})))))

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
    (d/transact! conn [msg])
    (recur)))

(defn dump-db [req res]
  (doto res
    (.writeHead 200 #js {"Content-Type" "application/edn"})
    (.write (pr-str @conn))
    (.end)))

(defn main [& _]
  (let [timer (every 20 (name :second) netwatch)
        server (.createServer http dump-db)]
    (.listen server 8080)))

(set! *main-cli-fn* main)