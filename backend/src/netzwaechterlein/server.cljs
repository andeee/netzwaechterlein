(ns ^:figwheel-always netzwaechterlein.server
  (:require [cljs.core.async :as async :refer [<! >!]]
            [datascript.core :as d]
            [cljs.nodejs :as nodejs])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce ping (js/require "net-ping"))
(defonce dns (js/require "dns"))
(defonce express (js/require "express"))
(defonce WebSocketServer (. (js/require "ws") -Server))
(defonce serve-static (js/require "serve-static"))
(defonce http (js/require "http"))
(defonce Database (. (js/require "sqlite3") -Database))

(nodejs/enable-util-print!)

(defonce conn (d/create-conn))

(defn send-watch [error type sensor-chan]
  (let [timestamp {:timestamp (.getTime (js/Date.))}
        merge-watch (partial merge {:type type} timestamp)]
    (if-not error
      (async/put! sensor-chan (merge-watch {:status :ok}))
      (async/put! sensor-chan (merge-watch {:status :error :message (str error)})))))

(defn create-sensor [pull-sensor-mult f]
  (let [pull-sensor-chan (async/chan)
        _ (async/tap pull-sensor-mult pull-sensor-chan)
        sensor-chan (async/chan)]
    (go-loop []
      (<! pull-sensor-chan)
      (f sensor-chan)
      (recur))
    sensor-chan))

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

(defn data->client [sensor-mult ws]
  (.send ws (pr-str @conn))
  (let [sensor-chan (async/chan)
        _ (async/tap sensor-mult sensor-chan)]
    (go-loop []
      (when-let [msg (<! sensor-chan)]
        (.send ws (pr-str msg)
               (fn [e] (when e (async/close! sensor-chan))))
        (recur)))))

(defn every [ms]
  (let [pull-chan (async/chan)]
    (go-loop []
      (<! (async/timeout ms))
      (>! pull-chan :kick-off)
      (recur))
    pull-chan))

(def app (express))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(def minute (* 60 1000))

(defn init-db [db]
  (.run db "CREATE TABLE IF NOT EXISTS netwatch (status text message text timestamp integer)"))

(defn setup-netwatch [{:keys [pull-chan sensor-fns publish-fns]}]
  (let [pull-sensor-mult (async/mult pull-chan)
        sensor (partial create-sensor pull-sensor-mult)
        sensor-mult (async/mult (async/merge (map sensor sensor-fns)))]
    (doseq [publish-fn publish-fns]
      (publish-fn sensor-mult))))

(defn publish-websocket [ws-server sensor-mult]
  (. ws-server (on "connection" (partial data->client sensor-mult))))

(defn -main [& _]
  (let [server (.createServer http app)
        ws-server (WebSocketServer. #js {:port 8081})
        db (init-db (Database. "netwatch.db"))]
    (setup-netwatch
     {:pull-chan (every minute)
      :sensors-fns [(partial ping-host "64.233.166.105")
                    (partial dns-lookup "www.google.com")]
      :publish-fns [(partial publish-websocket ws-server)]})
    (.listen server 8080)))

(set! *main-cli-fn* -main)
