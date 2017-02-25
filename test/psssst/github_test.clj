(ns psssst.github-test
  (:require [clojure.test :refer :all]
            [psssst.github :refer :all]))

(deftest creating-urls-test
  (testing "create a url for fetching pull requests"
    (let [url (pull-request-url "token" (->Repo "brianium" "psssst"))]
      (is (= (str api-url "/repos/brianium/psssst/pulls?access_token=token&state=open")
            url))))
  
  (testing "create a url for fetching repos"
    (let [url (repo-url "token" "netrivet")]
      (is (= (str api-url "/orgs/netrivet/repos?access_token=token")
             url)))))

(deftest pr->map-test
  (testing "mapping a pull request response to a more usable structure"
    (let [raw {:html_url "lol html"
               :assignee {:login "brian"}
               :user {:login "notbrian"}}
          mapped (pr->map raw)]
      (is (= "lol html" (:url mapped)))
      (is (= "brian" (:assignee mapped)))
      (is (= "notbrian" (:user mapped))))))

(deftest repo->Repo-test
  (testing "mapping a repo to a Repo record"
    (let [raw {:owner {:login "brian"}
               :name "psssst"}
          mapped (repo->Repo raw)]
      (is (= "brian" (:owner mapped)))
      (is (= "psssst" (:repo mapped))))))

(deftest needs-review?-test
  (testing "a pull request that needs review"
    (is (true? (needs-review? {:assignee nil}))))

  (testing "a pull request that does not need review"
    (is (false? (needs-review? {:assignee "brian"})))))

(deftest to-link-test
  (testing "mapping a single link to a map"
    (let [header "<url.com>; rel=\"next\""
          links (to-link header)]
      (is (= "url.com" (get links "next"))))))

(deftest get-next-url-test
  (testing "getting the next url when available"
    (let [headers {:link (str "<prev.com>; rel=\"prev\","
                              "<next.com>; rel=\"next\"")}
          url (get-next-url headers)]
      (is (= "next.com" url))))

  (testing "no next url returns nil"
    (let [headers {:link (str "<prev.com>; rel=\"prev\"")}
          url (get-next-url headers)]
      (is (nil? url))))

  (testing "no link header returns nil"
    (let [headers {:ham "sandwich"}
          url (get-next-url headers)]
      (is (nil? url)))))
