(ns netzwaechterlein.client
  (:require
   [rum.core :as rum]
   [datascript.core :as d]
   [cljs.reader :refer [read-string]]
   [chord.client :refer [ws-ch]]
   [cljs.core.async :as async :refer [<!]]
   [cljsjs.moment]
   [cljsjs.moment.locale.de])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn get-locale []
  (or (.. js/window -navigator -userLanguage)
      (.. js/window -navigator -language)))

(. js/moment (locale (get-locale)))

(defonce conn (d/create-conn))

(defn minute [entry]
  (-> (js/moment (:timestamp entry))
      (.milliseconds 0)
      (.seconds 0)
      (.toDate)
      (.getTime)))

(defn get-entries [db]
  (->> (d/q '[:find ?e
              :where [?e]] db)
       (map (fn [[e]] (d/entity db e)))
       (group-by minute)
       (sort)
       (reverse)))

(defn get-latest-by-status [db status]
  (->> (d/q '[:find ?e
              :in $ ?status
              :where [?e :status ?status]]
            db status)
       (map (fn [[e]] (d/entity db e)))
       (group-by minute)
       (sort)
       (reverse)))

(defn to-class [status]
  (if (= status :error) "danger" "success"))

(rum/defc response < rum/static [[at entries]]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"}
    (.calendar (js/moment at))]
   [:ul {:class "list-group"}
    (for [entry entries]
      [:li {:class (str "list-group-item list-group-item-" (to-class (:status entry)))}
       [:span (name (:type entry))]])]])

(rum/defc body < rum/static [db]
  [:div
   [:h1 "In the last 10 minutes"]
   (map response (take 10 (get-entries db)))
   [:h1 "Last errors"]
   (map response (take 10 (get-latest-by-status db :error)))])


(defn render-page
  ([] (render-page @conn))
  ([db]
   (rum/mount (body db) (.getElementById js/document "container"))))

(defonce data-ch (ws-ch (str "ws://" (.. js/window -location -hostname) ":8081")))

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
