(ns chaw.core
  (:require [yaml.core :as yaml]
            [cheshire.core :as cheshire]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure-ini.core :as ini]
            [toml.core :as toml])
  (:gen-class))

(defn write-output [m]
  (let [my-pretty-printer (cheshire/create-pretty-printer
                           (assoc cheshire/default-pretty-print-options
                                  :indent-arrays? true))]
    (println
     (cheshire/generate-string m {:pretty my-pretty-printer}))))

(defn read-input []
  (apply str
         (map (fn [s]
                (str s "\n"))
              (line-seq (java.io.BufferedReader. *in*)))))

(defn parse-yaml [yaml-string]
  (try
    (let [r (yaml/parse-string yaml-string)]
      (if (string? r) nil r))
    (catch Throwable _
      nil)))

(defn parse-xml [xml-string]
  (try
    (zip/xml-zip
     (xml/parse (java.io.ByteArrayInputStream. (.getBytes xml-string))))
    (catch Throwable _
      nil)))

(defn parse-json [json-string]
  (try
    (cheshire/parse-string json-string true)
    (catch Throwable _
      nil)))

(defn parse-ini [ini-string]
  (try
    (ini/read-ini (java.io.ByteArrayInputStream. (.getBytes ini-string)))
    (catch Throwable _
      nil)))

(defn parse-toml [toml-string]
  (try
    (toml/read toml-string)
    (catch Throwable _
      nil)))

(defn -main [& args]
  (System/setProperty "java.runtime.name" "Java(TM) SE GraalVM Runtime Environment")
  (let [input-str (read-input)]
    (write-output
     (first
      (filter identity
              (pmap #(% input-str)
                    [parse-xml
                     parse-yaml
                     parse-json
                     parse-ini
                     parse-toml]))))
    (System/exit 0)))
