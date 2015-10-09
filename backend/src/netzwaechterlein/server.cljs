(ns ^:figwheel-always netzwaechterlein.server
  (:require [cljs.core.async :as async :refer [<! >!]]
            [datascript.core :as d]
            [cljs.nodejs :as nodejs])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defonce ping (js/require "net-ping"))
(defonce dns (js/require "dns"))
(defonce express (js/require "express"))
(defonce WebSocketServer (. (js/require "ws") -Server))
(defonce serve-static (js/require "serve-static"))
(defonce http (js/require "http"))
(defonce Database (. (js/require "sqlite3") -Database))

(nodejs/enable-util-print!)

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

(defn create-publisher [f sensor-mult]
  (let [sensor-chan (async/chan)
        _ (async/tap sensor-mult sensor-chan)]
    (f sensor-chan)))

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

(defn row->clj [row]
  (-> (js->clj row :keywordize-keys true)
      (assoc :type (keyword (.-type row)))
      (assoc :status (keyword (.-status row)))))

(def sql->clj (map row->clj))

(defn dump-db [db]
  (let [dump-chan (async/chan)]
    (.all db
          "SELECT * FROM netwatch"
          (fn [err rows]
            (println err)
            (async/put! dump-chan (map row->clj rows))))
    dump-chan))

(defn data->client [db sensor-chan ws]
  (go
    (.send ws (pr-str (<! (dump-db db))))
    (loop []
      (when-let [msg (<! sensor-chan)]
        (.send ws (pr-str msg)
               (fn [e] (when e (async/close! sensor-chan))))
        (recur)))))

(defn every [ms]
  (let [pull-chan (async/chan (async/dropping-buffer 1))]
    (go-loop []
      (<! (async/timeout ms))
      (>! pull-chan :kick-off)
      (recur))
    pull-chan))

(def app (express))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(def minute (* 60 1000))

(defn init-db [db]
  (.run db "CREATE TABLE IF NOT EXISTS netwatch (type text, status text, message text, timestamp integer)"))

(defn setup-netwatch [{:keys [pull-chan sensor-fns publish-fns]}]
  (let [pull-sensor-mult (async/mult pull-chan)
        sensor (partial create-sensor pull-sensor-mult)
        sensor-mult (async/mult (async/merge (map sensor sensor-fns)))]
    (doseq [publish-fn publish-fns]
      (create-publisher publish-fn sensor-mult))))

(defn publish-websocket [db ws-server sensor-chan]
  (. ws-server (on "connection" (partial data->client db sensor-chan))))

(defn publish-db [db sensor-chan]
  (.serialize
   db
   #(do
      (init-db db)
      (go-loop []
        (when-let [{:keys [type status message timestamp]} (<! sensor-chan)]
          (.run db
                "INSERT INTO netwatch (type, status, message, timestamp) VALUES (?, ?, ?, ?)"
                (name type), (name status), message, timestamp)
          (recur))))))

(defn -main [& _]
  (let [server (.createServer http app)
        ws-server (WebSocketServer. #js {:port 8081})
        db (Database. "netwatch.db")]
    (setup-netwatch
     {:pull-chan (every minute)
      :sensors-fns [(partial ping-host "64.233.166.105")
                    (partial dns-lookup "www.google.com")]
      :publish-fns [(partial publish-websocket db ws-server)
                    (partial publish-db db)]})
    (.listen server 8080)))

(set! *main-cli-fn* -main)
