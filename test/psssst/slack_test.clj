(ns psssst.slack-test
  (:require [clojure.test :refer :all]
            [psssst.slack :refer :all]))

(deftest slack-url-test
  (testing "creating a slack url"
    (let [slack-url (url "token" "chat.postMessage")]
      (is (= (str api-url "/chat.postMessage?token=token")
             slack-url)))))
