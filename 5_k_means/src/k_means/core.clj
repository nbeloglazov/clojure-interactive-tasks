(ns k-means.core
  (:use quil.core))

(def w 800)
(def h 600)
(def d 10)
(def num-of-points 20)

(defmacro fn-state [vars & body]
  `(fn [] (let ~(vec (apply concat
                            (for [var vars]
                              [var `(state ~(keyword var))])))
            ~@body)))

(defn random-color []
  (->> #(+ 127 (rand-int 127))
       (repeatedly 3)
       (apply color)))

(def regenerate-points
  (fn-state [points initial-fn]
    (reset! points (initial-fn))))

(def colors (repeatedly random-color))

(def recalculate-clusters
  (fn-state [clusters points solution]
    (reset! clusters (solution @points))))

(defn setup-fn [initial-fn solution]
  (fn []
   (set-state!
    :clusters (atom [])
    :points (atom [])
    :solution solution
    :initial-fn initial-fn)
   (regenerate-points)
   (recalculate-clusters)))


(defn draw-cluster [points color]
  (fill color)
  (doseq [[x y] points]
    (ellipse x y d d)))

(def draw
  (fn-state [clusters]
    (background 200)
    (doseq [[cluster color] (map vector @clusters colors)]
      (draw-cluster cluster color))))

(def mouse-pressed
  (fn-state [points]
    (swap! points conj [(mouse-x) (mouse-y)])
    (recalculate-clusters)))

(def space (keyword " "))

(defn key-pressed []
  (when (= (key-as-keyword) space)
    (regenerate-points)
    (recalculate-clusters)))

(defn run [initial-fn solution]
  (sketch
   :title "k-means"
   :setup (setup-fn initial-fn solution)
   :draw draw
   :mouse-pressed mouse-pressed
   :key-pressed key-pressed
   :size [w h]))

(def run-empty (partial run #(vector)))

(defn random-points []
  (->> #(vector (rand-int w) (rand-int h))
       (repeatedly num-of-points)
       vec))

(run-empty #(map vector %))

