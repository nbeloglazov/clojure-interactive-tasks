(ns artillery.core
  (:use quil.core))

(def falling-speed [0 10])
(def missile-speed 10)
(def dead-dist 20)
(def target-size 10)
(def g [0 1])
(def missile-initial-pos [0 500])

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

(defn missile-speed-from-angle [angle]
  [(* (cos angle) missile-speed)
   (* (sin angle) missile-speed -1)])

(defn hit? [m-x m-y t-x t-y]
  (< (dist m-x m-y t-x t-y) dead-dist))

(defn angle [[x y]]
  (atan2 y x))

(defn launch-missile [{[p-x p-y] :pos type :type} {[t-x t-y] :pos} solution]
  (missile-speed-from-angle
   (if (= :static type)
     (solution)
     (solution p-x p-y t-x t-y))))

(defmacro fn-state [vars & body]
  `(fn [] (let ~(vec (apply concat
                            (for [var vars]
                              [var `(state ~(keyword var))])))
            ~@body)))

(defn update-target [target target-gen]
  (let [{:keys [status pos speed]} @target
        [x y] pos]
    (cond (or (nil? @target)
              (not (inside-border? x y)))
          (reset! target (target-gen))
          (= status :live)
          (advance target speed)
          :else
          (advance target falling-speed))))

(defn update-missile [target missile player solution]
  (let [[m-x m-y] (:pos @missile)
        [t-x t-y] (:pos @target)]
    (cond
      (not (:fired @target))
      (do (reset! missile {:pos (:pos @player)
                           :speed (launch-missile @player @target solution)})
          (swap! target assoc :fired true))
      (and (not (nil? m-x))
           (hit? m-x m-y t-x t-y))
      (do (reset! missile initial-missile)
          (swap! target assoc :status :dead))
      (not (or (nil? m-x) (inside-border? m-x m-y)))
      (do (reset! missile initial-missile)
          (swap! target assoc :fired false))
      (not (nil? m-x))
      (advance missile (:speed @missile)))))

(defn update-player [player]
  (when-not (apply inside-border? (:pos @player))
    (swap! player update-in [:speed 0] #(* % -1)))
  (advance player (:speed @player)))

(defn update-fn [solution]
  (fn-state [target missile target-gen player]
    (update-target target target-gen)
    (update-player player)
    (update-missile target missile player solution)))

(defn draw-plane [{:keys [status pos speed]}]
  (push-matrix)
  (translate pos)
  (->> (if (= status :live)
         speed
         falling-speed)
       angle
       rotate)
  (stroke-weight 3)
  (line -20 0 20 0)
  (line -25 -5 -20 0)
  (line -10 -5 5 0)
  (line -10 5 5 0)
  (pop-matrix))

(defn draw-missile [{:keys [pos speed]}]
  (when-not (nil? pos)
    (stroke-weight 2)
    (push-matrix)
    (translate pos)
    (rotate (angle speed))
    (line -10 0 10 0)
    (line -10 -2 -10 2)
    (line 2 2 10 0)
    (line 2 -2 10 0)
    (pop-matrix)))

(defn draw-player [{:keys [pos]}]
  (push-matrix)
  (translate pos)
  (stroke-weight 2)
  (line -8 0 8 0)
  (line -8 -5 -8 0)
  (line 8 -5 8 0)
  (line 0 -15 0 0)
  (line -2 -8 0 -15)
  (line 2 -8 0 -15)
  (pop-matrix))

(def draw
  (fn-state [target missile player]
    (background 200)
    (fill 0)
    ((:draw @target) @target)
    (draw-missile @missile)
    (draw-player @player)))

(defn setup [target-gen player]
  (smooth)
  (frame-rate 10)
  (->> (assoc initial-state
         :target-gen target-gen
         :player (atom player))
       seq
       flatten
       (apply set-state!)))

(defn run [target-gen player solution]
  (let [update (update-fn solution)
        update #(dotimes [_ 3] (update))]
    (sketch
     :title "Artillery"
     :setup (partial setup target-gen player)
     :draw #(do (update) (draw))
     :size [800 600])))

(run (fn [] {:pos [20 10]
             :speed [5 0]
             :status :live
             :draw draw-plane})
     {:pos [400 600]
      :speed [0 0]
      :type :dynamic}
     #_(fn [] (- (* 0.5 Math/PI) (Math/asin 0.5)))
     (fn [p-x p-y t-x t-y] (angle [(- t-x p-x) (- p-y t-y)])))