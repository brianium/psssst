(ns psssst.github
  (:require [clj-http.client :as client]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan close!]]))

(defrecord Repo [owner repo])

(defn pull-request-url
  "Given an Oauth token and a Repo record, create a url to request
   pull requests for the given repository"
  [token repo]
  (-> "https://api.github.com/repos/"
    (str (:owner repo) "/" (:repo repo) "/pulls")
      (str "?access_token=" token)
      (str "&state=open")))

(defn list-pull-requests
  "Given an Oauth token and a Repo record, fetch a set
  of json records representing open pull requests"
  [token repo]
  (-> (pull-request-url token repo)
      (client/get {:as :json})))

(defn pr->map
  "Convert a pull request to a map"
  [raw-pr]
  (-> {}
      (assoc :url (get raw-pr :url))
      (assoc :assignee (get-in raw-pr [:assignee :login]))
      (assoc :user (get-in raw-pr [:user :login]))))

(defn needs-review?
  "Returns true if the pull request has not been assigned to a user. False otherwise"
  [pr]
  (nil? (:assignee pr)))

(defn fetch-pull-requests
  "Given a collection of Repo records and an Oauth token, fetch all open pull requests
   that have not been assigned to someone"
  [repos token]
  (->> (pmap (partial list-pull-requests token) repos)
       (mapcat :body)
       (map pr->map)
       (filter needs-review?)))

(defstate github :start (chan)
                 :stop (close! chan))
