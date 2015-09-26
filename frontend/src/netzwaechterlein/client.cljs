(ns netzwaechterlein.client
  (:require
   [rum.core :as rum]
   [datascript.core :as d]
   [cljs.reader :refer [read-string]]
   [chord.client :refer [ws-ch]]
   [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce conn (d/create-conn))

(defn get-latest-entry [db type]
  (->> (d/q '[:find (max ?e) .
              :in $ ?type
              :where [?e :type ?type]]
            db type)
       (d/entity db)))

(rum/defc response < rum/static [entry]
  [:div (str "last " (name (:type entry)) " response: ")
   [:div
    (str
     (:timestamp entry)
     " - "
     (name (:status entry)))]])

(rum/defc body < rum/static [db]
  [:div
   (map #(-> % ((partial get-latest-entry db)) response) [:dns :ping])])

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
