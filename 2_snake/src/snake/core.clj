(ns snake.core
  (:use quil.core))

(def w 800)
(def h 600)
(def cell-size 20)
(def board-width (/ w cell-size))
(def board-height (/ h cell-size))
(def dirs {:right [1 0]
           :left [-1 0]
           :up [0 -1]
           :down [0 1]})

(defn to-real-coords [cell]
  (map #(* cell-size %) cell))

(defn draw-cell [draw-fn cell]
  (let [[real-x real-y] (to-real-coords cell)]
    (draw-fn real-x real-y cell-size cell-size)))

(defn neib-cell [cell dir]
  (let [[new-x new-y] (map + cell (dirs dir))]
    [(mod (+ new-x board-width) board-width)
     (mod (+ new-y board-height) board-height)]))

(defn rand-free-cell [{:keys [body]} apples walls]
  (let [body (set body)]
    (->> #(vector (rand-int board-width) (rand-int board-height))
         repeatedly
         (remove #(or (apples %)
                      (body %)
                      (walls %)))
         first)))

(defn dead? [head body walls]
  (boolean
   (or (walls head)
       (some #(= head %) body))))

(defmacro fn-state [vars & body]
  `(fn [] (let ~(vec (apply concat
                            (for [var vars]
                              [var `(state ~(keyword var))])))
            ~@body)))


(defn add-apple [snake apples walls]
  (let [new-apple (rand-free-cell @snake @apples @walls)]
    (swap! apples conj new-apple)))

(defn update-apples [snake apples walls]
  (dotimes [_ (- (:num (meta @apples))
                 (count @apples))]
    (add-apple snake apples walls)))

(defn update-snake [snake dir apples walls]
  (let [{:keys [body grow?]} @snake
        new-head (neib-cell (first body) dir)
        ate? (@apples new-head)
        new-body (if-not (dead? new-head body @walls)
                   (cons new-head
                         ((if (and ate? grow?) identity butlast) body))
                   [(rand-free-cell {:body []} @apples @walls)])]
    (swap! apples disj new-head)
    (swap! snake assoc :body new-body)))

(defn update-fn [solution]
  (fn-state [snake apples walls]
    (let [new-dir (solution (:body @snake)
                            @apples
                            @walls)]
      (update-snake snake new-dir apples walls))
    (update-apples snake apples walls)))

(defn draw-snake [{:keys [body]}]
  (fill 0 255 0)
  (doseq [cell body]
    (draw-cell rect cell)))

(defn draw-apples [apples]
  (fill 255 0 0)
  (doseq [apple apples]
    (draw-cell ellipse apple)))

(defn draw-walls [walls]
  (fill 139 69 19)
  (doseq [wall walls]
    (draw-cell rect wall)))

(def draw
  (fn-state [snake apples walls]
    (background 200)
    (ellipse-mode :corner)
    (draw-snake @snake)
    (draw-apples @apples)
    (draw-walls @walls)))

(defn setup-fn [num-of-apples walls grow?]
  (fn []
   (smooth)
   (set-state!
    :snake (atom {:body [[0 0]]
                  :grow? grow?})
    :walls (atom walls)
    :apples (atom (with-meta #{} {:num num-of-apples})))
   (->> [:snake :apples :walls] (map state) (apply update-apples))
   (frame-rate 10)))

(defn run [num-of-apples walls grow? solution]
  (let [update (update-fn solution)]
    (sketch
     :title "snake"
     :setup (setup-fn num-of-apples walls grow?)
     :draw #(do (update) (draw))
     :size [w h])))

(defn add-adjacent-wall [walls]
  (let [dir (rand-nth [:up :down :left :right])
        cell (rand-nth (seq walls))]
    (conj walls (neib-cell cell dir))))

(defn generate-walls-blot [size]
  (->> [board-width board-height]
       (map rand-int)
       vec
       hash-set
       (iterate add-adjacent-wall)
       (remove #(< (count %) size))
       first))


(defn make-move [snake-pos apple-pos]
  (cond (< (first snake-pos) (first apple-pos)) :right
        (> (first snake-pos) (first apple-pos)) :left
        (< (second snake-pos) (second apple-pos)) :down
        :else :up))

(defn run-not-grow [solution]
  (run 1 #{} false
       (fn [[head & _] apples _]
         (solution head (first apples)))))

(defn run-grow [solution]
  (run 1 #{} true
       (fn [snake apples _]
         (solution snake (first apples)))))

(defn run-many-apples [solution]
  (run 5 #{} true
       (fn [snake apples _]
         (solution snake apples))))

(defn run-with-walls [solution]
  (let [walls (->> #(generate-walls-blot 20)
                   (repeatedly 5)
                   (apply concat)
                   set)]
    (run 5 walls true solution)))
