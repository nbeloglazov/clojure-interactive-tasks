(ns artillery.work
  (:require [artillery.core :refer (plane-static plane-dynamic ufo-static ufo-dynamic)]))


;;; You goal is to hit plane my missile.
;;; Plane always starts at position x = 0, y = 500.
;;; Plane speed is equal to 5.
;;; Plane flies from the left to the right. So it's positions will be (0, 500), (5, 500), (10, 500), etc...
;;; You position is x = 400, y = 0.
;;; Missile speed is 10.
;;; Your goal is to calculate what angle you need to launch missile at in order to hit the plane.
;;; Your solution is a function that takes no parameters (constant function) and returns this angle.

;;; Here is an example of such function.
;;; It always returns PI/2 (missile is launched straight up).
;;; You can either calculate answer or find it by trying and adjusting different angles.
(defn plane-static-solution []
  (* 0.5 Math/PI))

;;; Here's a function that will show an  animation with plane flying and you launching missiles.
;;; You need to pass your solution (function name) to this function.
(plane-static plane-static-solution)



;;; Your goal is the same but now plane starts at random position.
;;; And you're also moving!.
;;; So only plane's speed and missiles' speed are constant now.
;;; You need to write a function that takes 4 numbers - your coordinates (player) and plane coordinates (target).
;;; Function should calculate angle to launch missile at.

;;; Example
;;; pl-x, pl-y - player's (your) coordinates.
;;; trg-x trg-y - target coordinates.
;;; Run and see how it launches missile now and then fix it to hit the plane.
(defn plane-dynamic-solution [pl-x pl-y trg-x trg-y]
  (Math/atan2 (- trg-y pl-y) (- trg-x pl-x)))

;;; To run program uncomment function and run it.
; (plane-dynamic plane-dynamic-solution)



;;; Now you need to hit UFO.
;;; You're lucky - it's not moving, just hanging in the air.
;;; But now gravity force is enabled so your missile won't fly in a straight way but rather in a curve. Remember Worms game? :)
;;; Gravity force decreases missile y speed by 0.1 every turn.
;;; UFO position x = 500, y = 300 (constants).
;;; UFO speed is equal to 0 (it's not moving).
;;; Your position x = 0, y = 0 (constants).
;;; Initial missile speed is 10.
;;; You need to write function that takes no arguments and returns angle.

;;; Now you don't have template function, so write one yourself.
;;; Hint: try to pass random angle at first e.g. 0.5 and see how it works.
;;; To check your solution use ufo-static function:
; (ufo-static YOUR_SOLUTION)



;;; Same UFO, but now it appears at random position (same as plane-dynamic).
;;; And you're moving now.
;;; Write a function that takes 4 arguments: your position (x, y)  and UFO's position (x, y) and returns an angle.
;;; To check your solution use ufo-dynamic function:
; (ufo-dynamic YOUR_SOLUTION)



;;; If you're still full of energy - add wind to simulation.
;;; Open core.clj file and try to figure out (it's not very easy) where missile speed is changed and try to add wind.
;;; And solve tasks with new obstacles.
