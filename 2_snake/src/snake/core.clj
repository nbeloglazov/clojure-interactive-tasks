(ns snake.core
  (:use quil.core))

(def cell-size 20)
(def dirs {:right [1 0]
           :left [-1 0]
           :up [0 -1]
           :down [0 1]})

(defn board-width []
  (quot (width) cell-size))

(defn board-height []
  (quot (height) cell-size))

(defn to-real-coords [cell]
  (map #(* cell-size %) cell))

(defn draw-cell [draw-fn cell]
  (let [[real-x real-y] (to-real-coords cell)]
    (draw-fn real-x real-y cell-size cell-size)))

(defn neib-cell [cell dir]
  (let [[new-x new-y] (map + cell (dirs dir))
        w (board-width)
        h (board-height)]
    [(mod (+ new-x w) w)
     (mod (+ new-y h) h)]))

(defn rand-free-cell [{:keys [body]} apples]
  (let [w (board-width)
        h (board-height)
        body (set body)]
    (->> #(vector (rand-int w) (rand-int h))
         repeatedly
         (remove #(or (apples %)
                      (body %)))
         first)))

(defmacro fn-state [vars & body]
  `(fn [] (let ~(vec (apply concat
                            (for [var vars]
                              [var `(state ~(keyword var))])))
            ~@body)))



(defn update-apples [snake apples]
  (if (empty? @apples)
    (swap! apples conj (rand-free-cell @snake @apples))))

(defn update-snake [snake apples dir]
  (let [{:keys [body grow?]} @snake
        new-head (neib-cell (first body) dir)
        ate? (@apples new-head)
        new-body (cons new-head
                       ((if (and ate? grow?) identity butlast) body))]
    (swap! apples disj new-head)
    (swap! snake assoc :body new-body)))

(defn update-fn [solution]
  (fn-state [snake apples]
    (let [new-dir (solution (first (:body @snake))
                            (first @apples))]
      (update-snake snake apples new-dir))
    (update-apples snake apples)))

(defn draw-snake [{:keys [body]}]
  (fill 0 255 0)
  (doseq [cell body]
    (draw-cell rect cell)))

(defn draw-apples [apples]
  (fill 255 0 0)
  (doseq [apple apples]
    (draw-cell ellipse apple)))

(def draw
  (fn-state [snake apples]
    (background 200)
    (ellipse-mode :corner)
    (draw-snake @snake)
    (draw-apples @apples)))

(defn setup []
  (smooth)
  (set-state!
   :snake (atom {:body [[0 0]]
                 :grow? true})
   :apples (atom #{(rand-free-cell {:body [[0 0]]} #{})}))
  (frame-rate 10))

(defn run [solution]
  (let [update (update-fn solution)]
    (sketch
     :title "snake"
     :setup setup
     :draw #(do (update) (draw))
     :size [800 600])))


(defn make-move [snake-pos apple-pos]
  (cond (< (first snake-pos) (first apple-pos)) :right
        (> (first snake-pos) (first apple-pos)) :left
        (< (second snake-pos) (second apple-pos)) :down
        :else :up))

(run make-move)

