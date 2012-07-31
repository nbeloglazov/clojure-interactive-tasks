(ns {{name}}.core
  (:use quil.core))

(def initial-state {:iteration (atom 0)})

(defn map-by-fn [fn keys]
  (into {} (map #(vector % (fn %)) keys)))

(defn state-to-map []
  (map-by-fn state (keys initial-state)))

(defn setup []
  (smooth)
  (apply set-state! (flatten (seq initial-state)))
  (frame-rate 10))

(defn update-fn [solution]
  (fn [{:keys [iteration]}]
    (swap! iteration + (solution))))

(defn draw [{:keys [iteration]}]
  (background 200)
  (fill 0)
  (ellipse @iteration @iteration 10 10))


(defn run [solution]
  (let [update (update-fn solution)]
    (sketch
      :title "{{name}}"
      :setup setup
      :draw #(doto (state-to-map) update draw)
      :size [800 600])))

