(defproject psssst "0.1.0-SNAPSHOT"
  :description "Notify your team that code needs review"
  :url "http://github.com/brianium/psssst"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src"]
  
  :dependencies [[clj-http "2.3.0"]
                 [cheshire "5.7.0"]
                 [org.clojure/core.async "0.2.395"]
                 [mount "0.1.11"]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]}})
