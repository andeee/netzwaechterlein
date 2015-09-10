(ns netzwaechterlein.client
  (:require [rum]
            [datascript :as d]
            [goog.net.XhrIo]
            [cljs.reader :refer [read-string]]))

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

(defn get-data []
  (.send goog.net.XhrIo "http://localhost:8080/data"
         (fn [e]
           (let [db (read-string (.getResponseText (.-target e)))]
             (reset! conn db)
             (render-page)))))

(get-data)
