(ns netzwaechterlein.server
  (:require [cljs.core.async :as async :refer [<!]]
            [datascript :as d])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce every (js/require "every-moment"))
(defonce ping (js/require "net-ping"))
(defonce dns (js/require "dns"))
(defonce express (js/require "express"))
(defonce WebSocketServer (.-Server (js/require "ws")))
(defonce serve-static (js/require "serve-static"))
(defonce http (js/require "http"))

(enable-console-print!)

(def nw-ch (async/chan))

(def nw-mult (async/mult nw-ch))

(defonce conn (d/create-conn))

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

(def app (express))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(defn -main [& _]
  (let [timer (every 20 (name :second) netwatch)
        server (.createServer http app)
        websocket-server (WebSocketServer. #js {:port 8081})]
    (.listen server 8080)
    (. websocket-server (on "connection" data->client))))

(set! *main-cli-fn* -main)
