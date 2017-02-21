(ns psssst.core
  (:require [mount.core :as mount]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [psssst.poll :refer [poll]])
  (:import java.io.File)
  (:gen-class))

(defn read-config
  "Reads the given path into a clojure data structure"
  [path]
  (binding [*read-eval* false]
    (-> (slurp path)
        (read-string))))

(defn usage
  "Returns a usage summary for psssst"
  [options-summary]
  (->> ["Pssst polls for pull requests in need of review."
        ""
        "Usage: psssst [options]"
        ""
        "Options:"
        options-summary
        ""
        "See https://github.com/brianium/psssst for more information."]
        (string/join \newline)))

(defn exit
  "Exit with the given code and message"
  [status msg]
  (println msg)
  (System/exit status))

(defn validate
  "Enures the configuration has all required keys"
  [config errors]
  (let [keys [:github-token :slack-token :users :org]]
    (cond
      (not (every? (partial contains? config) keys)) (conj errors "Configuration is invalid")
      :else errors)))

(def cli-options
  [["-c" "--config FILE" "psssst config file"
    :default (str
               (System/getProperty "user.dir")
               (File/separator)
               "psssst.clj")]
   ["-h" "--help"]])

(defn parse-config
  "Creates a configuration from cli options"
  [options]
  (try
    (read-config (:config options))
    (catch Exception e {})))

(defn error-msg
  "Return an error message string"
  [errors]
  (str "Psssst encountered the following errors:\n\n"
    (string/join \newline errors)))

(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)
        config (parse-config options)
        errors (validate config errors)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (mount/start)
    (.addShutdownHook (Runtime/getRuntime)
      (Thread. mount/stop))
    (poll config)))
