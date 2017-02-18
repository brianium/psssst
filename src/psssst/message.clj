(ns psssst.message)

(defn message
  [pr config]
  (fn [[github-login slack-name]]
    {:channel (str "@" slack-name)
     :text (str (:url pr) " needs review")
     :author (:user pr)
     :token (:slack-token config)}))

(defn not-author?
  [users message]
  (not= (subs (:channel message) 1)
        (get users (:author message))))

(defn normalize-message
  [message]
  (dissoc message :author))

(defn messages
  [{:keys [users] :as config}]
  (fn [pr]
    (->> (map (message pr config) users)
         (filter (partial not-author? users))
         (map normalize-message))))
