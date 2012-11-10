(ns durak.core
  (:use quil.core
        durak.logic
        [clojure.java.io :only (resource)])
  (:import [java.awt.event KeyEvent]))

(def margin-top 20)
(def margin-left 200)
(def hand-cards-gap 40)
(def overlap 50)
(def hand-table-gap 30)
(def table-cards-gap 10)
(def card-height 96)
(def card-width 72)

(defmacro fn-state [vars & body]
  `(fn [] (let ~(vec (apply concat
                            (for [var vars]
                              [var `(state ~(keyword var))])))
            ~@body)))

(defn card-url [{:keys [rank suit]}]
  (let [name (str (name suit) "_" rank ".png")
        res (resource name)]
    (if (nil? res)
      (throw (IllegalArgumentException. (str "Could not find image " name)))
      res)))

(defn load-images []
  (let [cards-images (into {} (map #(vector % (load-image (card-url %))) deck))]
    (assoc cards-images :back (load-image (resource "blue_back.png")))))

(defn setup-fn [player-a player-b]
  (fn []
    (smooth)
    (set-state! :images (load-images)
                :state (atom (init-game player-a player-b)))
    (frame-rate 10)))

(defn draw-player [player images top]
  (doseq [[ind card] (map-indexed vector player)]
    (when-not (nil? card)
      (image (images card) (+ margin-left (* ind hand-cards-gap)) top))))

(defn draw-table [table attacker images]
  (let [pairs (partition-all 2 table)
        y-top-card (+ margin-top card-height hand-table-gap)
        y-bottom-card (+ y-top-card (- card-height overlap))]
    (doseq [[ind [attack-card defend-card]] (map-indexed vector pairs)]
      (let [x (+ margin-left (* ind (+ table-cards-gap card-width)))
            [y-at y-def] ((if (zero? attacker) identity reverse) [y-top-card y-bottom-card])]
        (image (images attack-card) x y-at)
        (when-not (nil? defend-card)
          (image (images defend-card) x y-def))))))

(defn draw-deck [deck images]
  (let [y-deck (+ margin-top
                  (/ (- (+ (* 4 card-height)
                      (* 2 hand-table-gap))
                   overlap
                   card-height)
                2))
        x (/ (- margin-left card-height card-width) 2)
        y-tramp (+ y-deck (/ (- card-height card-width) 2))]
    (when-let [tramp (last deck)]
      (push-matrix)
      (translate x y-tramp)
      (rotate (/ Math/PI -2))
      (image (images tramp) (- card-width) (/ card-width 2))
      (pop-matrix)
      (dotimes [ind (dec (count deck))]
        (image (images :back) x (- y-deck (* ind 2)))))))

(def draw
  (fn-state [images state]
    (background 200)
    (fill 0)
    (let [{:keys [players table attacker deck]} @state
          [pl-a pl-b] players]
      (draw-player pl-a (constantly (:back images)) margin-top)
      (draw-player pl-b images (+ margin-top
                                  (* 3 card-height)
                                  (- overlap)
                                  (* 2 hand-table-gap)))
      (draw-table table attacker images)
      (draw-deck deck images))))

(def key-pressed
  (fn-state [state]
    (cond (= KeyEvent/VK_SPACE (key-code))
          (do (swap! state next-action))
          (= KeyEvent/VK_R (key-code))
          (reset! state (apply init-game (:player-fns @state))))))

(defn run [player-a player-b]
  (let [setup (setup-fn player-a player-b)]
    (sketch
     :title "Durak"
     :setup setup
     :draw draw
     :key-pressed key-pressed
     :size [800 600])))

(def simple-bot
  {:attack
   (fn [{:keys [table hand tramp]}]
     (if (empty? table)
          (first hand)
          nil))
   :defend
   (fn [{:keys [table hand tramp]}]
     (first (filter #(higher? % (last table) tramp) hand)))})

(def run-game (partial run simple-bot))
