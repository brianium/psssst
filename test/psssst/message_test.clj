(ns psssst.message-test
  (:require [clojure.test :refer :all]
            [psssst.message :refer :all]))

(deftest message-test
  (testing "creating a message mapper from users and pr"
    (let [mapper (message {:user "brianium" :url "pr.com"} {:slack-token "token"})
          msg (mapper ["brianium" "brian"])]
      (is (= "@brian" (:channel msg)))
      (is (= "pr.com needs review" (:text msg)))
      (is (= "brianium" (:author msg)))
      (is (= "token" (:token msg)))
      (is (= ":robot_face:" (:icon_emoji msg)))
      (is (= "Psssst" (:username msg)))))

  (testing "creating a message overriding defaults"
    (let [mapper (message {:user "brianium" :url "pr.com"} {:slack-token "token"
                                                            :icon-emoji ":dancers:"
                                                            :username "Dance Bot"})
          msg (mapper ["brianium" "brian"])]
      (is (= ":dancers:" (:icon_emoji msg)))
      (is (= "Dance Bot" (:username msg))))))

(deftest not-author?-test
  (testing "when author is different"
    (let [users {"brianium" "brian"}
          message {:channel "@brian" :author "jaredh159"}]
      (is (true? (not-author? users message)))))

  (testing "when author is same"
    (let [users {"brianium" "brian"}
          message {:channel "@brian" :author "brianium"}]
      (is (false? (not-author? users message))))))

(deftest normalize-message-test
  (testing "stripping fields not required to send message"
    (let [msg {:channel "@brian" :author "brianium"}
          normalized (normalize-message msg)]
      (is (false? (contains? normalized :author))))))

(deftest messages-test
  (testing "creating messages for the given users"
    (let [users {"brianium" "brian" "jaredh159" "jared"}
          pr {:url "pr.com" :user "brianium"}
          msgs (messages {:users users :slack-token "token"})
          mapped (msgs pr)]
      (is (= 1 (count mapped)))
      (is (= (:channel (first mapped)) "@jared")))))
