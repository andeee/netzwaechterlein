(ns netzwaechterlein.server
  (:require [cljs.core.async :as async :refer [<!]]
            [datascript :as d]
            [figwheel.client :as fw])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce every (js/require "every-moment"))
(defonce ping (js/require "net-ping"))
(defonce dns (js/require "dns"))
(defonce express (js/require "express"))
(defonce serve-static (js/require "serve-static"))
(defonce http (js/require "http"))

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
  (.send res (pr-str @conn)))

(def app (express))

(. app (get "/data" dump-db))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(defn main [& _]
  (let [timer (every 20 (name :second) netwatch)]
    (doto (.createServer http #(app %1 %2))
      (.listen 8080))))

(set! *main-cli-fn* main)

(fw/start { })
