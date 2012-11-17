(ns fourclj-grabber.core
  (:require [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [tentacles.gists :as gists]))

(def base-url "http://www.4clojure.com/")
(def problems-file "problems.csv")
(def solutions-file "solutions_%d.clj")
(def scores-file "scores.clj")

(def lesson-1 [1 2 14 15 16 35 36 42 162 166])
(def lesson-2 [17 18 64 71 24 32 61 50 67 77])
(def lesson-3 [26 28 34 39 83 126 65 69 121 79])
(def lesson-6 [23 27 30 33 41 95 107 135 102 158])
(def lesson-7 [74 106 108 120 146])
(def lesson-9 [118 31 45 153 171 148 114 104 75 117])

(def users
  (set (map name
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
          kolina
          nikelandjelo
          mashabarashko])))

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
  {:user (-> (html/select sol [:.solution-username]) first html/text string/lower-case (string/replace #"'s solution:" ""))
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

(defn get-solutions-filter [problem logn password users]
  (->> (login logn password)
       (get-solutions problem logn)
       (filter #(users (:user %)))))

(defn get-solutions-as-file
  ([problem logn password users]
     (build-content problem (get-solutions-filter problem logn password users)))
  ([problem logn password]
     (get-solutions-as-file problem logn password (fn [_] true))))

(defn find-gist [gist-name auth]
  (->> (gists/user-gists (re-find #"[^:]*" auth) {:auth auth})
       (filter #(.contains (:description %) gist-name))
       first))

(defn map-values [f m]
  (into {} (for [[k v] m] [k (f k v)])))

(defn write-solutions-to-gist [{:keys [problems users github-auth gist-name login-4clj password-4clj]}]
  (let [solutions (into {} (pmap #(get-solutions-as-file % login-4clj password-4clj users) problems))]
    (do
      (if-let [gist (find-gist gist-name github-auth)]
        (let [solutions (map-values #(hash-map :content %2) solutions)]
          (gists/edit-gist (:id gist) {:auth github-auth :files solutions}))
        (gists/create-gist solutions {:auth github-auth :description gist-name :public false}))
      nil)))

(defn update-lesson [tasks name github-pass fclj-pass]
  (write-solutions-to-gist
   {:problems tasks
    :users users
    :github-auth (format "nbeloglazov:%s" github-pass)
    :gist-name name
    :login-4clj "nikelandjelo"
    :password-4clj fclj-pass}))

(defn update-lesson-2 [github-pass fclj-pass]
  (update-lesson lesson-2 "Lesson 2" github-pass fclj-pass))

(defn update-lesson-3 [github-pass fclj-pass]
  (update-lesson lesson-3 "Lesson 3" github-pass fclj-pass))

(defn update-lesson-6 [github-pass fclj-pass]
  (update-lesson lesson-6 "Lesson 6" github-pass fclj-pass))

(defn update-lesson-7 [github-pass fclj-pass]
  (update-lesson lesson-7 "Lesson 7" github-pass fclj-pass))

(defn update-all-lessons [github-pass fclj-pass]
  (doseq [fns [update-lesson-2 update-lesson-3 update-lesson-6 update-lesson-7]]
    (println "Do!")
    (fns github-pass fclj-pass)))

(defn get-scores [fclj-name fclj-pass & task-colls]
  (letfn [(users-for-problem [problem]
            (let [users (map :user (get-solutions-filter problem fclj-name fclj-pass users))]
              (zipmap users (repeat #{problem}))))
          (map-value [f m]
            (into {} (for [[k v] m] [k (f v)])))
          (count-intersections [tasks]
            (->> (map set task-colls)
                 (map #(set/intersection % tasks))
                 (map count)))]
    (->> (apply concat task-colls)
         (pmap users-for-problem)
         (apply merge-with set/union)
         (map-value count-intersections))))

(defn write-scores [fclj-name fclj-pass & task-colls]
  (let [data (read-string (slurp scores-file))
        new-scores (apply get-scores fclj-name fclj-pass task-colls)]
    (->> (conj data [new-scores (java.util.Date.)])
         prn-str
         (spit scores-file))))

(defn show-difference-last []
  (let [all (read-string (slurp scores-file))
        [latest prev] (map first (reverse all))
        names (set (concat (keys latest) (keys prev)))]
    (doseq [name (sort names)]
      (when (not= (latest name) (prev name))
        (println name)
        (println (prev name))
        (println (latest name))
        (println \newline)))))

