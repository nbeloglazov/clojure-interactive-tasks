(ns derivative.core
  (:use [incanter.charts :only (add-function function-plot)]
        [incanter.core :only (view sin cos log)]))

(defn functions-plot [min-x max-x f & fns]
  (reduce #(add-function % %2 min-x max-x)
          (function-plot f min-x max-x)
          fns))

(def ^:dynamic x)

(defn calc-expr [expr x-value]
  (binding [x x-value]
    (eval expr)))

(defn expr-plot [min-x max-x & fns]
  (apply functions-plot min-x max-x (map #(partial calc-expr %) fns)))



;;; Task is to implement derivative function.
;;; It takes expression - valid clojure form like (+ x x) or (* 5 (+ x (* 7 x))) and returns derivative of this expression.
;;; All functions in expression are binary or unary, so (* x x x) is not allowed. (* (* x x) x) will be passed instead.
;;; derivative should support following functions: +, -, *, / - binary, sin, cos, log - unary.
;;; It must return valid clojure expression.
;;; Examples:
;;; (derivative '(+ x x)) => 2 or '(+ 1 1)
;;; (derivative '(sin (* 2 x))) => (* 2 (cos (* 2 x))) or it's equilavent.

(defn derivative [expr]
  expr)


;;; Tests.
;;; Uncomment and try every function.

;;; f = x^2
(defn test-simple []
  (let [f '(* x x)]
    (view (expr-plot -10 10 f (derivative f)))))

;(test-simple)



;;; f = x^2
(defn test-second-derivative []
  (let [f '(* x x)]
    (view (expr-plot -10 10 f (derivative f) (derivative (derivative f))))))

;(test-second-derivative)



;;; f = -7x^2 + 2x + 5
(defn test-complex []
  (let [f '(+ (+ (* -7 (* x x))
                 (* 2 x))
              5)]
    (view (expr-plot -5 5 f (derivative f) (derivative (derivative f))))))

;(test-complex)



;;; f = 1/(1 + x^2)
(defn test-division []
  (let [f '(/ 1 (+ (* x x)
                   1))]
    (view (expr-plot -4 4 f (derivative f)))))

;(test-division)



;;; f = sin(2x)
(defn test-trigonometry []
  (let [f '(sin (* 2 x))]
    (view (expr-plot (- Math/PI) Math/PI  f (derivative f) (derivative (derivative f))))))

;(test-trigonometry)



;;; f = log(1 + x^2)
(defn test-logarithm []
  (let [f '(log (+ 1 (* x x)))]
    (view (expr-plot -4 4 f (derivative f)))))

;(test-logarithm)