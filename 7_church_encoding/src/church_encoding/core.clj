(ns church-encoding.core)

(defn to-normal-num [n]
  ((n inc) 0))

(defn to-church-num [n]
  (fn [f] (fn [x] (nth (iterate f x) n))))

(defn run-tests [pass? log-result tests]
  (let [check-and-log (fn [test]
                        (let [result (pass? test)]
                          (log-result (if result "OK" "Fail") test)
                          result))
        passed (->> tests
                    (map check-and-log)
                    (filter true?)
                    count)]
    (printf "%d/%d passed" passed (count tests))))

(defn test-plus [plus]
  (run-tests
   (fn [[a b]]
     (= (to-normal-num
         ((plus (to-church-num a)) (to-church-num b)))
        (+ a b)))
   (fn [result [a b]]
     (printf "%4s  %d + %d = %d\n" result a b (+ a b)))
   [[0 0]
    [0 1]
    [1 0]
    [2 3]
    [10 10]]))

(defn test-mult [mult]
  (run-tests
   (fn [[a b]]
     (= (to-normal-num
         ((mult (to-church-num a)) (to-church-num b)))
        (* a b)))
   (fn [result [a b]]
     (printf "%4s  %d * %d = %d\n" result a b (* a b)))
   [[0 0]
    [1 0]
    [1 1]
    [2 2]
    [3 6]]))


(defn test-pow [pow]
  (run-tests
   (fn [[a b]]
     (= (to-normal-num
         ((pow (to-church-num a)) (to-church-num b)))
        (int (Math/pow a b))))
   (fn [result [a b]]
     (printf "%4s  %d ^ %d = %d\n" result a b (int (Math/pow a b))))
   [[1 0]
    [1 1]
    [2 2]
    [2 3]
    [0 2]]))

(defn test-dec [dec]
  (run-tests
   (fn [n]
     (= (to-normal-num
         (dec (to-church-num n)))
        (- n 1)))
   (fn [result n]
     (printf "%4s  (dec %d) = %d\n" result n (- n 1)))
   [1 2 3 4 5]))

(defn test-sum [sum]
  (run-tests
   (fn [n]
     (= (to-normal-num (sum (to-church-num n)))
        (apply + (range (inc n)))))
   (fn [result n]
     (printf "%4s  (dec %d) = %d\n" result n (apply + (range (inc n)))))
   [0 1 2 3 4 5]))


(defn test-list [{:keys [empty? empty-list cons head tail]}]
  (letfn [(to-lambda-list [coll]
            (reduce #((cons %2) %1) empty-list (reverse coll)))
          (to-vec [lambda-list]
            (((empty? lambda-list)
              (delay []))
             (delay (clojure.core/cons (head lambda-list)
                                       @(to-vec (tail lambda-list))))))
          (to-word [result]
            (if result "  OK" "Fail"))]
    (let [res [(let [res (((empty? empty-list) true) false)]
                 (println (to-word res) "(empty? empty-list) must be true")
                 res)
               (let [lamdba-list ((cons 42) empty-list)
                     h (head lamdba-list)
                     res (= h 42)]
                 (println (to-word res) "(head (cons 42 empty-list)) = 42")
                 res)
               (let [lambda-list ((cons 42) empty-list)
                     t (tail lambda-list)
                     res (((empty? t) true) false)]
                 (println (to-word res) "(empty? (tail (cons 42 empty-list))) must be true")
                 res)
               (let [lambda-list (to-lambda-list [1 2 3 4 5])
                     res (= [1 2 3 4 5] @(to-vec lambda-list))]
                 (println (to-word res) "converting [1 2 3 4 5] to lambda list and back to clojure vector using passed functions")
                 res)]
          passed (count (filter true? res))]
      (printf "%d/%d passed\n" passed (count res)))))

(defn test-map-reduce [{:keys [empty? empty-list cons head tail map reduce]}]
  (letfn [(to-lambda-list [coll]
            (clojure.core/reduce #((cons %2) %1) empty-list (reverse coll)))
          (to-vec [lambda-list]
            (((empty? lambda-list)
              (delay []))
             (delay (clojure.core/cons (head lambda-list)
                                       @(to-vec (tail lambda-list))))))
          (to-word [result]
            (if result "  OK" "Fail"))]
    (let [l (to-lambda-list [1 2 3 4 5])
          res [(let [res-v @(to-vec ((map #(* % %)) l))
                     res (= [1 4 9 16 25] res-v)]
                 (println (to-word res) "(map square [1 2 3 4 5]) must [1 4 9 16 25]")
                 (when-not res
                   (println "     Got" res-v))
                 res)
               (let [mult (fn [a] (fn [b] (* a b)))
                     res-v (((reduce mult) 1) l)
                     res (= res-v 120)]
                 (println (to-word res) "(reduce * 1 [1 2 3 4 5]) must be 120")
                 (when-not res
                   (println "     Got" res-v))
                 res)]
          passed (count (filter true? res))]
      (printf "%d/%d passed\n" passed (count res)))))

