(ns psssst.core-test
  (:require [clojure.test :refer :all]
            [psssst.core :refer :all]))

(deftest validate-config-test
  (testing "validating config without errors"
    (is (empty? (validate {:github-token "token"
                           :slack-token "slack"
                           :users {"brianium" "brian"}
                           :org "netrivet"} []))))

  (testing "validating config with errors"
    (is (not (empty? (validate {:github-token "missing-others"} []))))))
