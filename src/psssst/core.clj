(ns psssst.core
  (:require [clojure.core.async :refer [go >! <! go-loop put!]]
            [mount.core :as mount]
            [psssst.github :refer [github fetch-pull-requests]]
            [psssst.slack :refer [slack post-message]]
            [psssst.message :refer [messages]]
            [psssst.schedule :refer [schedule]]))

(defn notify
  [pull-requests config]
  (let [msgs (map (messages config) pull-requests)]
    (loop [coll msgs]
      (when-let [msg (first coll)]
        (put! slack msg)
        (recur (rest coll))))))

(defn check-pull-requests
  [{:keys [github-token org] :as config}]
  (go (>! github (fetch-pull-requests github-token org)))
  (go (notify (<! github) config)))

(defn post-messages []
  (go-loop []
    (let [msg (<! slack)]
      (clojure.pprint/pprint msg)
      (recur))))

(defn poll
  [config]
  (mount/start)
  (schedule check-pull-requests config)
  (post-messages))
