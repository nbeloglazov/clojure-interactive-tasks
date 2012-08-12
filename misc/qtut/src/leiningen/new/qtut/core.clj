(ns {{name}}.core
  (:use quil.core))

(def initial-state {:iteration (atom 0)})

(defmacro fn-state [vars & body]
  `(fn [] (let ~(vec (apply concat
                            (for [var vars]
                              [var `(state ~(keyword var))])))
            ~@body)))

(defn setup []
  (smooth)
  (apply set-state! (flatten (seq initial-state)))
  (frame-rate 10))

(defn update-fn [solution]
  (fn-state [iteration]
            (swap! iteration + (solution))))

(def draw
  (fn-state [iteration]
   (background 200)
   (fill 0)
   (ellipse @iteration @iteration 10 10)))


(defn run [solution]
  (let [update (update-fn solution)]
    (sketch
      :title "{{name}}"
      :setup setup
      :draw #(do  (update) (draw))
      :size [800 600])))

