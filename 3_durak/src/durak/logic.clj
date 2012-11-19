(ns durak.logic)

(def deck
  (for [suit [:spades :hearts :diamonds :clubs]
        rank (range 6 15)]
    {:suit suit
     :rank rank}))

(defn op [attacker]
  (- 1 attacker))

(defn draw-cards-player [player deck]
  (let [need (max (min (- 6 (count player))
                       (count deck))
                  0)]
    [(concat player (take need deck))
     (drop need deck)]))

(defn higher? [{suit-a :suit rank-a :rank}
               {suit-b :suit rank-b :rank}
               trump]
  (if (= suit-a suit-b)
    (> rank-a rank-b)
    (= suit-a trump)))

(defn draw-cards [{:keys [players deck attacker] :as state}]
  (let [players (mapv #(remove nil? %) players)
        [pl1 deck] (draw-cards-player (players attacker) deck)
        [pl2 deck] (draw-cards-player (players (op attacker)) deck)]
    (-> state
        (assoc-in [:deck] deck)
        (assoc-in [:players attacker] pl1)
        (assoc-in [:players (op attacker)] pl2))))

(defn init-game [player-a player-b]
  (let [deck (shuffle deck)]
    (draw-cards {:player-fns [player-a player-b]
                 :table []
                 :deck deck
                 :trump (:suit (last deck))
                 :attacker (rand-int 2)
                 :players [[] []]})))

(defn discard [{:keys [table players attacker] :as state}]
  {:pre [(even? (count table))]}
  (-> state
      (assoc-in [:table] [])
      (update-in [:attacker] op)
      draw-cards))

(defn defender-takes [{:keys [table players attacker] :as state}]
  {:pre [(odd? (count table))]}
  (-> state
      (assoc-in [:table] [])
      (update-in [:players (op attacker)] concat table)
      draw-cards))

(defn contains-rank? [table {:keys [rank]}]
  (some #(= rank (:rank %)) table))

(defn call-player-fn [type {:keys [attacker players player-fns table trump]}]
  (let [player-ind (if (= type :attack) attacker (op attacker))]
    ((get-in player-fns [player-ind type])
     {:table table
      :hand (remove nil? (players player-ind))
      :trump trump})))

(defn throw-iae [& args]
  (throw (IllegalArgumentException. (apply str args))))

(defn validate-attack-card [card {:keys [attacker players table]}]
  (when-not (or (empty? table)
                (contains-rank? table card))
    (throw-iae "Can't attack with "
               card
               ". There is no such rank on the table: "
               table))
  (when (every? #(not= card %) (players attacker))
    (throw-iae "Can't attack with "
         card
         ". Attacker doesn't have such card: "
         (players attacker))))

(defn validate-defend-card [card {:keys [attacker players table trump]}]
  (when (every? #(not= card %) (players (op attacker)))
    (throw-iae "Can't defend with "
               card
               ". Defender doesn't have such card: "
               (players (op attacker))))
  (when-not (higher? card (last table) trump)
    (throw-iae "Can't defend with "
               card
               ". It's lower than attack card: "
               (last table))))

(defn move-to-table [card player state]
  (-> state
      (update-in [:table] conj card)
      (update-in [:players player] #(replace {card nil} %))))

(defn end-of-game? [{:keys [table players deck]}]
  (and (empty? deck)
       (some empty? players)
       (empty? table)))


(defn next-action [{:keys [table attacker players] :as state}]
  (if (end-of-game? state)
    ;;; Return state without modification
    state
    ;;; Check if this attack of defense.
    (if (even? (count table))
      ;;; Attack
      (if (->> (op attacker) players (remove nil?) empty?)
        ;;; Defender has no cards. End of attack
        (discard state)
        ;;; Defender has at least 1 card. Attack continues.
        (if-let [card (call-player-fn :attack state)]
          ;;; Attacker selected card for attack.
          (do (validate-attack-card card state)
              (move-to-table card attacker state))
          ;;; Attacker select no card for attach. End of attack.
          (discard state)))
     ;;; Defend
     (if-let [card (call-player-fn :defend state)]
       ;;; Defender selected card for defense.
       (do (validate-defend-card card state)
           (move-to-table card (op attacker) state))
       ;;; Defender selected no card for defense. He takes all cards from the table.
       (defender-takes state)))))

