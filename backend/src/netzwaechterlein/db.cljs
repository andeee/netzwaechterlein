(ns netzwaechterlein.db
  (:require [cljs.core.async :refer [<! chan put!]])
    (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn row->clj [row]
  (let [result-row (js->clj row :keywordize-keys true)]
    (-> result-row
        (assoc :type (keyword (:type result-row)))
        (assoc :status (keyword (:status result-row))))))

(def sql->clj (map row->clj))

(defn dump-db [db]
  (let [dump-chan (chan)]
    (.all db
          "SELECT * FROM netwatch"
          (fn [err rows]
            (println err)
            (put! dump-chan (map row->clj rows))))
    dump-chan))

(defn init-db [db]
  (.run db "CREATE TABLE IF NOT EXISTS netwatch (type text, status text, message text, timestamp integer)"))

(defn publish-db [db sensor-chan]
  (.serialize db #(init-db db))
  (go-loop []
    (when-let [{:keys [type status message timestamp]} (<! sensor-chan)]
      (.run db
            "INSERT INTO netwatch (type, status, message, timestamp) VALUES (?, ?, ?, ?)"
            (name type), (name status), message, timestamp)
      (recur))))
