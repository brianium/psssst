(ns psssst.schedule
  (:require [mount.core :refer [defstate]])
  (:import (java.util.concurrent Executors
                                 TimeUnit)))

(defstate executor :start (Executors/newScheduledThreadPool 1)
                   :stop (.shutdown executor))

(defn schedule
  "Executes the given function at an interval specified in seconds.

  The function will be called with the configuration passed to schedule.
  
  The default interval is 600 seconds - or every 10 minutes"
  [fn {:keys [interval] :or {interval 600} :as config}]
  (.scheduleAtFixedRate executor
    (partial fn config) 0 interval TimeUnit/SECONDS))
