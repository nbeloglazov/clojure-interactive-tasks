(ns church-encoding.core)

(defn to-normal-num [n]
  ((n inc) 0))

(defn to-church-num [n]
  (if (zero? n)
    (fn [_] identity)
    (fn [f] (fn [x]
              (f (((to-church-num (dec n)) f) x))))))

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
        (Math/pow a b)))
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

