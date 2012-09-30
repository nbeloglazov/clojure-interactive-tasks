(ns fourclj-grabber.core
  (:require [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]))

(def base-url "http://www.4clojure.com/")
(def problems-file "problems.csv")
(def solutions-file "solutions_%d.clj")

(def users
  (map name
       '[cgrand
         megaterik
         kabbi
         anjenson
         hlebiksi
         zayankovsky
         lanakomar
         quadrocube
         pavelfromby
         efimikvitaliy
         kolina]))

(defn string-to-html [str]
  (->> (.getBytes str)
       (java.io.ByteArrayInputStream.)
       (net.cgrand.xml/parse)))

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

(defn login [login password]
  (->> (client/post
        (str base-url "login")
        {:form-params {:user login :pwd password}})
       :cookies))



(defn extract-solution [sol]
  {:user (-> (html/select sol [:.solution-username]) first html/text)
   :solution (-> (html/select sol [:pre]) first html/text)})

(defn get-solutions [problem cookies]
  (-> (client/get
          (str base-url "/problem/solutions/" problem)
          {:cookies cookies})
       :body
       string-to-html
       (html/select [:.follower-solution])
       (#(map extract-solution %))))

(defn write-solutions [problem solutions]
  (let [{:keys [description tests]} (get-problem problem)
        #_description #_(-> description string-to-html html/texts)]
    (->> (map #(str ";" (:user %) \newline (:solution %) \newline \newline) solutions)
         (string/join \newline)
         (str ";" description \newline
              (string/join \newline tests)
              \newline \newline \newline)
         (spit (format solutions-file problem)))))

(defn matches? [{:keys [user]} allowed-users]
  (if (empty? allowed-users)
    true
    (some #(.contains user %) allowed-users)))

(defn get-and-write-solutions
  ([problem logn password users]
     (->> (login logn password)
          (get-solutions problem)
          (filter #(matches? % users))
          (write-solutions problem)))
  ([problem logn password]
     (get-and-write-solutions [])))

