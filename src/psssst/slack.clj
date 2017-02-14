(ns psssst.slack
  (:require [clj-http.client :as client]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan close!]]))

(defonce api-url "https://slack.com/api")

(defn url
  "Create an authenticated url for the slack api"
  [token method]
  (-> (str api-url "/" method)
      (str "?token=" token)))

(defn list-channels
  "List all available channels"
  [token]
  (-> (url token "channels.list")
      (client/get {:as :json})))

(defn post-message
  "Post a message to a slack channel"
  [token fields]
  (-> (url token "chat.postMessage")
      (client/get {:query-params fields
                   :as :json})))

(defstate slack :start (chan)
                :stop (close! slack))
