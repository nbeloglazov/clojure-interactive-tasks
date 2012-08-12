(ns durak.logic)

(def deck
  (for [suit [:spades :hearts :diamonds :clubs]
        rank (range 2 15)]
    {:suit suit
     :rank rank}))

(defn draw-cards-player [player deck]
  (let [need (min (- 6 (count player))
                  (count deck))]
    [(concat player (take need deck))
     (drop need deck)]))

(defn draw-cards [{:keys [players deck attacker] :as state}]
  (let [[pl1 deck] (draw-cards-player (players attacker) deck)
        [pl2 deck] (draw-cards-player (players (- 1 attacker)) deck)]
    (-> state
        (assoc-in [:deck] deck)
        (assoc-in [:players attacker] pl1)
        (assoc-in [:players (- 1 attacker)] pl2))))

(defn init-game [player-a player-b]
  (let [deck (shuffle deck)]
    (draw-cards {:player-fns [player-a player-b]
                 :table []
                 :deck deck
                 :tramp (:suit (last deck))
                 :attacker (rand-int 2)
                 :players [[] []]})))
