(ns netzwaechterlein.client
  (:require
   [rum]
   [datascript :as d]
   [cljs.reader :refer [read-string]]
   [chord.client :refer [ws-ch]]
   [cljs.core.async :as async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce conn (d/create-conn))

(defn get-latest-entry [type]
  (->> (d/q '[:find (max ?e) .
              :in $ ?type
              :where [?e :type ?type]]
            @conn type)
       (d/entity @conn)))

(rum/defc response < rum/static [type]
  (let [entry (get-latest-entry type)]
    [:div (str "last " (name type) " response: ")
     [:div
      (str
       (:timestamp entry)
       " - "
       (name (:status entry)))]]))

(rum/defc body < rum/static []
  [:div
   (response :dns)
   (response :ping)])

(defn render-page []
  (rum/mount (body) (.-body js/document)))

(def data-ch (ws-ch "ws://localhost:8081"))

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
