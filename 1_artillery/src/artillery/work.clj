(ns artillery.work
  (:use [artillery.core :only (plane-static plane-dynamic ufo-static ufo-dynamic)]))


(defn plane-static-solution []
  (* 0.5 Math/PI))

(plane-static plane-static-solution)




(defn plane-dynamic-solution [p-x p-y t-x t-y]
  (Math/atan2 (- p-y t-y) (- t-x p-x)))

; (plane-dynamic plane-dynamic-solution)




; (ufo-static YOUR_SOLUTION)




; (ufo-dynamic YOUR_SOLUTION)

