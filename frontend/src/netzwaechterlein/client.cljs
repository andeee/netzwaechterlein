(ns netzwaechterlein.client
  (:require
   [rum.core :as rum]
   [datascript.core :as d]
   [cljs.reader :refer [read-string]]
   [chord.client :refer [ws-ch]]
   [cljs.core.async :as async :refer [<!]]
   [cljsjs.moment])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce conn (d/create-conn))

(defn minute [entry]
  (-> (js/moment (:timestamp entry))
      (.milliseconds 0)
      (.seconds 0)
      (.toDate)
      (.getTime)))

(defn get-latest-entry [db type]
  (->> (d/q '[:find (max ?e) .
              :in $ ?type
              :where [?e :type ?type]]
            db type)
       (d/entity db)))

(defn get-entries [db]
  (->> (d/q '[:find ?e
              :where [?e]] db)
       (map (fn [[e]] (d/entity db e)))
       (group-by minute)
       (sort)
       (reverse)))

(rum/defc response < rum/static [[at entries]]
  [:div
   (.calendar (js/moment at))
   [:ul
    (for [entry entries]
      [:li {:class (name (:status entry))}
       [:span
        (name (:type entry)) " response: "
        (name (:status entry))]])]])

(rum/defc body < rum/static [db]
  [:div
   [:h1 "In the last 10 minutes"]
   (map response (take 10 (get-entries db)))])

(defn render-page
  ([] (render-page @conn))
  ([db] (rum/mount (body db) (.-body js/document))))

(def data-ch (ws-ch (str "ws://" (.. js/window -location -hostname) ":8081")))

(d/listen!
 conn :render
 (fn [tx-report]
   (render-page (:db-after tx-report))))

(go
  (let [{:keys [ws-channel error]} (<! data-ch)]
    (when-not error
      (reset! conn (:message (<! ws-channel)))
      (render-page)
      (loop []
        (let [{:keys [message error]} (<! ws-channel)]
          (when-not error
            (d/transact! conn [message])
            (recur)))))))
