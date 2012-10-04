(ns fourclj-grabber.core
  (:require [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [tentacles.gists :as gists]))

(def base-url "http://www.4clojure.com/")
(def problems-file "problems.csv")
(def solutions-file "solutions_%d.clj")
(def lesson-1 [1 2 14 15 16 35 36 42 162 166])
(def lesson-2 [17 18 64 71 24 32 61 50 67 77])

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

(defn get-solutions [problem name cookies]
  (let [html (-> (client/get
                   (str base-url "/problem/solutions/" problem)
                   {:cookies cookies})
                 :body
                 string-to-html)
        following-solutions (map extract-solution (html/select html [:.follower-solution]))
        my-solution {:user name
                     :solution (-> (html/select html [[:pre html/first-of-type]] ) first html/text)}]
    (cons my-solution following-solutions)))

(defn build-content [problem solutions]
  (let [{:keys [description tests]} (get-problem problem)]
    (->> (map #(str ";" (:user %) \newline (:solution %) \newline \newline) solutions)
         (string/join \newline)
         (str ";" description \newline
              (string/join \newline tests)
              \newline \newline \newline)
         (apply str)
         (hash-map (format solutions-file problem)))))

(defn write-solutions-to-file [problem solutions]
  (spit (format solutions-file problem) (build-content problem solutions)))

(defn matches? [{:keys [user]} allowed-users]
  (if (empty? allowed-users)
    true
    (some #(.contains user %) allowed-users)))

(defn get-solutions-as-file
  ([problem logn password users]
     (->> (login logn password)
          (get-solutions problem logn)
          (filter #(matches? % users))
          (build-content problem)))
  ([problem logn password]
     (get-solutions-as-file problem logn password [])))

(defn find-gist [gist-name auth]
  (->> (gists/user-gists (re-find #"[^:]*" auth) {:auth auth})
       (filter #(.contains (:description %) gist-name))
       first))

(defn map-values [f m]
  (into {} (for [[k v] m] [k (f k v)])))

(defn write-solutions-to-gist [{:keys [problems users github-auth gist-name login-4clj password-4clj]}]
  (let [solutions (into {} (pmap #(get-solutions-as-file % login-4clj password-4clj users) problems))]
    (if-let [gist (find-gist gist-name github-auth)]
      (let [solutions (map-values #(hash-map :content %2) solutions)]
        (gists/edit-gist (:id gist) {:auth github-auth :files solutions}))
      (gists/create-gist solutions {:auth github-auth :description gist-name :public false}))))

(defn update-lesson-2 [github-pass fclj-pass]
  (write-solutions-to-gist
   {:problems lesson-2
    :users users
    :github-auth (format "nbeloglazov:%s" github-pass)
    :gist-name "Lesson 2"
    :login-4clj "Nikelandjelo"
    :password-4clj fclj-pass}))

