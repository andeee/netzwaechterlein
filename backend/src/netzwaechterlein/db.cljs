(ns netzwaechterlein.db
  (:require [cljs.core.async :refer [<! chan put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn dissoc-nil-vals [row]
  (apply dissoc
         row
         (for [[k v] row :when (nil? v)] k)))

(defn row->clj [row]
  (let [result-row (js->clj row :keywordize-keys true)]
    (-> result-row
        (assoc :type (keyword (:type result-row)))
        (assoc :status (keyword (:status result-row)))
        dissoc-nil-vals)))

(def sql->clj (map row->clj))

(defn days [days] (* 1000 * 60 * 24 * days))

(defn dump-db [db]
  (let [dump-chan (chan)
        seven-days-ago (- (.getTime (js/Date.)) (days 7))]
    (.all db
          "SELECT * FROM netwatch WHERE timestamp >= ?" seven-days-ago
          (fn [err rows]
            (if err
              (do (close! dump-chan) (println err))
              (put! dump-chan (map row->clj rows)))))
    dump-chan))

(defn init-db [db]
  (let [init-sql "CREATE TABLE IF NOT EXISTS netwatch (type text, status text, message text, timestamp integer)"]
    (.serialize db #(.run db init-sql))))

(defn publish-db [db sensor-chan]
  (init-db db)
  (go-loop []
    (when-let [{:keys [type status message timestamp]} (<! sensor-chan)]
      (.run db
            "INSERT INTO netwatch (type, status, message, timestamp) VALUES (?, ?, ?, ?)"
            (name type), (name status), message, timestamp)
      (recur))))
