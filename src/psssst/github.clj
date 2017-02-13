(ns psssst.github
  (:require [clj-http.client :as client]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan close!]]))

(defonce api-url "https://api.github.com")

(defrecord Repo [owner repo])

(defn pull-request-url
  "Given an Oauth token and a Repo record, create a url to request
   pull requests for the given repository"
  [token repo]
  (-> (str api-url "/repos/")
      (str (:owner repo) "/" (:repo repo) "/pulls")
      (str "?access_token=" token)
      (str "&state=open")))

(defn repo-url
  "Given an Oauth token and an org name - get a url to
   to request repositories for the organization"
  [token org]
  (-> (str api-url "/orgs/" org "/repos")
      (str "?access_token=" token)))

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

(defn repo->Repo
  "Convert a github repository record into a Repo record"
  [raw-repo]
  (-> {}
      (assoc :owner (get-in raw-repo [:owner :login]))
      (assoc :repo (get raw-repo :name))
      map->Repo))

(defn needs-review?
  "Returns true if the pull request has not been assigned to a user. False otherwise"
  [pr]
  (nil? (:assignee pr)))

(defn list-repos
  "List all repositories for the given organization"
  [token org]
  (->> (client/get (repo-url token org) {:as :json})
       :body
       (map repo->Repo)))

(defn fetch-pull-requests
  "Given a collection of Repo records and an Oauth token, fetch all open pull requests
   that have not been assigned to someone"
  [org token]
  (let [repos (list-repos token org)]
    (->> (pmap (partial list-pull-requests token) repos)
         (mapcat :body)
         (map pr->map)
         (filter needs-review?))))

(defstate github :start (chan)
                 :stop (close! chan))
