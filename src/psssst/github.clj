(ns psssst.github
  (:require [clj-http.client :as client]
            [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan close!]]
            [clojure.string :refer [split]]))

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

(defn pr->map
  "Convert a pull request to a map"
  [raw-pr]
  (-> {}
      (assoc :url (get raw-pr :html_url))
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

(defn to-link
  "Converts a github link header to a map"
  [header-val]
  (->> (re-find #"<([^>]+)>; rel=\"(.*)\"" header-val)
       (reverse)
       (take 2)
       (apply hash-map)))

(defn get-next-url
  "Get the url for the next set or recrods if available"
  [headers]
  (when (contains? headers :link)
    (->> (split (:link headers) #",")
         (map to-link)
         (apply merge)
         (#(get %1 "next")))))

(defn list-repos
  "List all repositories for the given organization"
  [token org]
  (loop [url (repo-url token org)
         responses []]
    (let [response (client/get url {:as :json})
          headers (:headers response)
          next-url (get-next-url headers)]
      (if next-url
        (recur next-url (conj responses response))
        (->> (mapcat :body (conj responses response))
             (map repo->Repo))))))

(defn list-pull-requests
  "Given an Oauth token and a Repo record, fetch a set
  of json records representing open pull requests"
  [token repo]
  (-> (pull-request-url token repo)
      (client/get {:as :json})))

(defn fetch-pull-requests
  "Given a collection of Repo records and an Oauth token, fetch all open pull requests
   that have not been assigned to someone"
  [token org]
  (let [repos (list-repos token org)]
    (->> (pmap (partial list-pull-requests token) repos)
         (mapcat :body)
         (map pr->map)
         (filter needs-review?))))

(defstate github :start (chan)
                 :stop (close! github))
