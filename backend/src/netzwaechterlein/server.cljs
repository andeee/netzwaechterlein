(ns ^:figwheel-always netzwaechterlein.server
  (:require [cljs.nodejs :as nodejs]
            [netzwaechterlein.sensors :refer [ping-host dns-lookup]]
            [netzwaechterlein.core :refer [every setup-netwatch]]
            [netzwaechterlein.websocket :refer [publish-websocket]]
            [netzwaechterlein.db :refer [publish-db]]))

(defonce express (js/require "express"))
(defonce WebSocketServer (. (js/require "ws") -Server))
(defonce serve-static (js/require "serve-static"))
(defonce http (js/require "http"))
(defonce Database (. (js/require "sqlite3") -Database))

(nodejs/enable-util-print!)

(def app (express))

(. app (use (serve-static "resources/public" #js {:index "index.html"})))

(def minute (* 60 1000))

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
