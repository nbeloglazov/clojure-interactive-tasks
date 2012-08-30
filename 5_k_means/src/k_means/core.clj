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

(defn circle-points [[x y] rad n]
  (let [random (java.util.Random.)
        dist-fn #(* (.nextGaussian random) 0.3333333 rad)]
    (repeatedly n #(let [angle (rand (* 2 Math/PI))
                         dist (dist-fn)]
                     [(+ x (* (cos angle) dist))
                      (+ y (* (sin angle) dist))]))))

(def regenerate-points
  (fn-state [points initial-fn]
    (reset! points (vec (initial-fn)))))

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

(defn n-circles [n max-rad min-rad n-points]
  (letfn [(random-circle []
            (let [r (+ min-rad (rand-int (- max-rad min-rad)))]
              [(+ r (rand-int (- w r r)))
               (+ r (rand-int (- h r r)))
               r]))
          (intersects? [[x0 y0 r0]
                        [x1 y1 r1]]
            (< (dist x0 y0 x1 y1) (+ r0 r1)))
          (intersects-any? [circles circle]
            (some #(intersects? circle %) circles))
          (add-circle [circles]
            (->> (repeatedly 100 random-circle)
                 (remove #(intersects-any? circles %))
                 (#(nth % 0 (random-circle)))
                 (conj circles)))
          (circle-to-points [[x y r]]
            (circle-points [x y] r n-points))]
    (->> (nth (iterate add-circle []) n)
         (map circle-to-points)
         (apply concat)
         #_shuffle)))

(def run-empty (partial run #(vector)))

(def run-2-circles (partial run (partial n-circles 2 100 200 30)))

(def run-3-circles (partial run (partial n-circles 3 100 200 30)))

(def run-random-circles (partial run #(n-circles (+ 2 (rand-int 5)) 50 100 30)))

(defn random-points []
  (->> #(vector (rand-int w) (rand-int h))
       (repeatedly num-of-points)))

(defn stupid [points]
  (map vector points))



