(defproject psssst "0.1.0-SNAPSHOT"
  :description "Notify your team that code needs review"
  :url "http://github.com/brianium/psssst"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src"]

  :main psssst.core
  
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-http "2.3.0"]
                 [cheshire "5.7.0"]
                 [org.clojure/core.async "0.2.395"]
                 [mount "0.1.11"]]

  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-binplus "0.6.2"]
                             [com.jakemccrary/lein-test-refresh "0.18.1"]]}}

  :bin {:name "psssst"})
