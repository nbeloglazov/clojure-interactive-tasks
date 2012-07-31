(ns artillery.core
  (:use quil.core))

(def target-speed [5 0])
(def falling-speed [2 20])
(def missile-speed 10)
(def dead-dist 20)
(def target-size 10)
(def g [0 1])
(def missile-initial-pos [0 500])

(def initial-target {:status :live
                     :pos [10 10]})

(def initial-missile {:pos nil
                      :speed nil})

(def initial-state {:target (atom nil)
                    :missile (atom initial-missile)})

(defn inside-border? [x y]
  (and (<= 0 x (width))
       (<= 0 y (height))))

(defn advance [object speed]
  (println @object speed)
  (swap! object update-in [:pos] #(map + % speed)))

(defn update-target [target]
  (let [{:keys [status pos]} @target
        [x y] pos]
    (cond (or (nil? @target)
              (not (inside-border? x y)))
          (reset! target initial-target)
          (= status :live)
          (advance target target-speed)
          :else
          (advance target falling-speed))))

(defn missile-speed-from-angle [angle]
  [(* (Math/cos angle) missile-speed)
   (* (Math/sin angle) missile-speed -1)])

(defn hit? [m-x m-y t-x t-y]
  (< (dist m-x m-y t-x t-y) dead-dist))

(defn update-missile [target missile solution]
  (let [[m-x m-y] (:pos @missile)
        [t-x t-y] (:pos @target)]
    (cond (and (not (nil? m-x))
               (hit? m-x m-y t-x t-y))
         (do (reset! missile initial-missile)
             (swap! target assoc :status :dead))
         (= initial-target @target)
         (reset! missile {:pos missile-initial-pos
                          :speed (missile-speed-from-angle (solution))})
         (not (or (nil? m-x) (inside-border? m-x m-y)))
         (reset! missile initial-missile)
         (not (nil? m-x))
         (advance missile (:speed @missile)))))

(defn update-fn [solution]
  (fn [{:keys [target missile]}]
    (update-target target)
    (update-missile target missile solution)))

(defn draw-target [{:keys [status pos]}]
  (push-matrix)
  (translate pos)
  (stroke-weight 3)
  (line -20 0 20 0)
  (line -25 -5 -20 0)
  (line -10 -5 5 0)
  (line -10 5 5 0)
  (pop-matrix))

(defn draw-missile [{:keys [pos]}]
  (when-not (nil? pos)
    (push-matrix)
    (translate pos)
    (line -10 0 10 0)
    (line -10 -2 -10 2)
    (line 2 2 10 0)
    (line 2 -2 10 0)
    (pop-matrix)))

(defn draw [{:keys [target missile]}]
  (background 200)
  (fill 0)
  (draw-target @target)
  (draw-missile @missile))



(defn map-by-fn [fn keys]
  (into {} (map #(vector % (fn %)) keys)))

(defn state-to-map []
  (map-by-fn state (keys initial-state)))

(defn setup []
  (smooth)
  (apply set-state! (flatten (seq initial-state))))

(defn run [solution]
  (let [update (update-fn solution)
        update #(dotimes [_ 3] (update %))]
    (sketch
      :title "artillery"
      :setup setup
      :draw #(doto (state-to-map) update draw)
      :size [800 600])))


(run (fn [] (- (* 0.5 Math/PI) (Math/asin 0.5))))