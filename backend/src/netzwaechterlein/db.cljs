(ns netzwaechterlein.db
  (:require [cljs.core.async :as async :refer [<!]])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

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

(defn init-db [db]
  (.run db "CREATE TABLE IF NOT EXISTS netwatch (type text, status text, message text, timestamp integer)"))

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
