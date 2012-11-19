(ns church-encoding.work
  (:use [church-encoding.core]))

;;; Task is to implement arithmetic on Church numerals.
;;; Check this page: http://en.wikipedia.org/wiki/Church_encoding
;;; You can use utility function to-church-num and to-normal-num to convert normal to church and church to normal:
;;; Note that to-church-num returns function that takes 1 argument (f)
;;; and returns function that takes 1 argument (x) that calculates (f (f ... (f x)...))
;;; All functions in this task must 1 argument functions that return other functions.

;;; Example:

(def church-5 (to-church-num 5))    ; 5 in church numerals

(defn print-star [x] (print "*") x) ; Takes 1 argument, prints a star and retuns argument without modification.

((church-5 print-star) nil)         ; Prints ***** to console

(to-normal-num church-5)            ; returns 5

(def church-2 (to-church-num 2))    ; we'll use it in examples later



;;; Implement + (plus) for church numerals.

(def plus :YOUR_IMPLEMENTATION_HERE)

(to-normal-num ((plus church-2) church-2)) ; must return 4

(test-plus plus) ; test your solution



;;; Implement * (multiplication) for church numerals

(def mult :YOUR_IMPLEMENTATION_HERE)

(to-normal-num ((mult church-2) church-5)) ; must return 10

(test-mult mult) ; test your solution



;;; Implement ^ (pow function) for church numerals.

(def pow :YOUR_IMPLEMENTATION_HERE)

(to-normal-num ((pow church-2) church-5)) ; must return 32

(test-pow pow) ; test your solution



;;; Implement dec function for church numerals.

(def dec :YOUR_IMPLEMENTATION_HERE)

(to-normal-num (dec church-5)) ; must return 4

(test-dec dec) ; test your solution



;;; Implement sum function. sum takes number n and returns sum of all numbers less or equals to n.
;;; You'll need to use recursion here. For recursion you'll need lazy values.
;;; You can use delay for that: http://clojuredocs.org/clojure_core/1.2.0/clojure.core/delay

(def sum :YOUR_IMPLEMENTATION_HERE)

(to-normal-num (sum church-2)) ; must return 3

(test-sum sum)



;;; Additional task.
;;; Implement set of function to create/manipulate lists.
;;; Your need to implement following functions:
;;; empty? - checks if list is empty, returns true or false. see church booleans http://en.wikipedia.org/wiki/Church_encoding#Church_booleans
;;; empty-list - used as "end" of the list.
;;; head - returns head of a list
;;; tail - returns tail of a list
;;; cons - takes 2 arguments h and t, and creates a list such that (head (cons a b)) = a, (tail (cons a b)) = b
;;;
;;; Help: http://en.wikipedia.org/wiki/Church_encoding#List_encodings

(def empty? :YOUR_IMPLEMENTATION_HERE)

(def empty-list :YOUR_IMPLEMENTATION_HERE)

(def head :YOUR_IMPLEMENTATION_HERE)

(def tail :YOUR_IMPLEMENTATION_HERE)

(def cons :YOUR_IMPLEMENTATION_HERE)

(((empty? empty-list) true) false) ; must return true

(head (cons "Hello" empty-list)) ; must return "Hello"

(let [list (cons "Hello" empty-list)
      t (tail list)]
  ((empty? t) true) false) ; must return true

(test-list {:empty? empty?
            :empty-list empty-list
            :head head
            :tail tail
            :cons cons}) ; test your solution



