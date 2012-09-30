(ns fourclj-grabber.core
  (:require [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-http.client :as client]))

(def base-url "http://www.4clojure.com/")
(def problems-file "problems.csv")

(defn get-problem [id]
  (try
    (->> (str base-url "/api/problem/" id)
         client/get
         :body
         json/read-json
         (#(assoc % :id id)))
    (catch Exception e nil)))

(defn fields-for-csv [problem]
  (map problem [:id :title :difficulty :description :tests]))

(defn write-csv [file csv]
  (with-open [writer (io/writer file)]
    (csv/write-csv writer csv)))

(defn write-problems-to-csv []
  (->> (range 200)
       (map get-problem)
       (remove nil?)
       (map fields-for-csv)
       (write-csv problems-file)))

