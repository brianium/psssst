(ns psssst.poll
  (:require [clojure.core.async :refer [go >! <! go-loop put!]]
            [psssst.github :refer [github fetch-pull-requests]]
            [psssst.slack :refer [slack post-message]]
            [psssst.message :refer [messages]]
            [psssst.schedule :refer [schedule]]))

(defn notify
  "Maps pull requests to message records and puts them on the slack output
   channel"
  [pull-requests config]
  (let [msg-coll (map (messages config) pull-requests)]
    (loop [coll msg-coll]
      (when-let [msgs (first coll)]
        (doseq [msg msgs]
          (put! slack msg))
        (recur (rest coll))))))

(defn check-pull-requests
  "Checks for reviewable pull requests and notifies relevant
   channels"
  [{:keys [github-token org] :as config}]
  (go (>! github (fetch-pull-requests github-token org)))
  (go (notify (<! github) config)))

(defn post-messages
  "Post messages to slack as they become available"
  [{:keys [slack-token]}]
  (go-loop []
    (let [msg (<! slack)]
      (post-message slack-token msg)
      (recur))))

(defn poll
  "Polls for pull requests based on the configured interval"
  [config]
  (schedule check-pull-requests config)
  (post-messages config))
