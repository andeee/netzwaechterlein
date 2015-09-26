(ns netzwaechterlein.server
  (:require [cljs.core.async :as async :refer [<!]]
            [datascript.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce ping (js/require "net-ping"))
(defonce dns (js/require "dns"))
(defonce express (js/require "express"))
(defonce WebSocketServer (. (js/require "ws") -Server))
(defonce serve-static (js/require "serve-static"))
(defonce http (js/require "http"))
(defonce Database (. (js/require "sqlite3") -Database))

(enable-console-print!)

(def nw-ch (async/chan))

(def nw-mult (async/mult nw-ch))

(defonce conn (d/create-conn))

(defn send-watch [error type sensor-chan]
  (let [timestamp {:timestamp (.getTime (js/Date.))}
        merge-watch (partial merge {:type type} timestamp)]
    (if-not error
      (async/put! nw-ch (merge-watch {:status :ok}))
      (async/put! nw-ch (merge-watch {:status :error :message (str error)})))))

(defn add-sensor [pull-mult sensor-chan f]
  (let [pull-chan (async/chan)
        _ (async/tap pull-mult pull-chan)]
    (go-loop []
      (<! pull-chan)
      (f sensor-chan)
      (recur))))

(defn ping-host [address sensor-chan]
  (let [session (.createSession ping)]
    (.pingHost
     session
     address
     (fn [error _]
       (send-watch error :ping sensor-chan)
       (.close session)))))

(defn dns-lookup [address sensor-chan]
  (.resolve
   dns
   address
   (fn [error _ _] (send-watch error :dns sensor-chan))))

(defn copy-nw-ch []
  (let [nw-copy (async/chan)]
    (async/tap nw-mult nw-copy)
    nw-copy))

(let [nw-event (copy-nw-ch)]
  (go-loop []
    (d/transact! conn [(<! nw-event)])
    (recur)))

(defn data->client [ws]
  (.send ws (pr-str @conn))
  (let [nw-event (copy-nw-ch)]
    (go-loop []
      (when-let [msg (<! nw-event)]
        (.send ws (pr-str msg)
               (fn [e] (when e (async/close! nw-event))))
        (recur)))))

(defn every [ms]
  (async/timeout ms))

(def app (express))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(def minute (* 60 1000))

(defn -main [& _]
  (let [pull-mult (async/mult (every minute))
        server (.createServer http app)
        websocket-server (WebSocketServer. #js {:port 8081})
        db (Database. "netwatch.db")
        add (partial add-sensor pull-mult nw-ch)]
    (add (partial ping-host "64.233.166.105"))
    (add (partial dns-lookup "www.google.com"))
    (.run db "CREATE TABLE IF NOT EXISTS netwatch (status text message text timestamp integer)")
    (.listen server 8080)
    (. websocket-server (on "connection" data->client))))

(set! *main-cli-fn* -main)
