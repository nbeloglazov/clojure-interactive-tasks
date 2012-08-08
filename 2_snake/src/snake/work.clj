(ns snake.work
  (:use [snake.core :only (run-not-grow run-grow run-many-apples run-with-walls)]))

;;; You're writing a bot for playing snake.
;;; So, you are a snake and your goal is to collect apples.
;;; Field sizes: 40 x 30
;;; Every turn you move to one of the neighbours cell.
;;; Your function must take 2 arguments: snake's position and apple's position and decide which direction to move.
;;; Directions are: :up, :down, :left, :right (they are keywords). Your function must return one of these directions.
;;; Position (snake's or apple's) is a vector of 2 elements: x and y.
;;; In this task snake is not growing after it ate an apple so there is no danger of snake hitting itself.
;;; Note: upper left corner cell is (0, 0).

;;; Uncomment and substitute your solution
; (run-not-grow YOUR_SOLUTION_HERE)



;;; Snake grows now (each time snake eats an apple, it's length increases).
;;; You need to write similar function as in previous task.
;;; It takes 2 arguments.
;;; First argument is snake's body - collection of cells, each cell is a vector of x and y. First cell is snake's head.
;;; Second argument is apple's position - vector of x and y.
;;; It should return direction: :up, :down, :left or :right.
;;; Note that you cannot change direction to the opposite in 1 move: snake will hit it's tail if length is 2 or more.
;;; Wait, you can change direction but snake will die :\

;;; Uncomment and substitute your solution
; (run-grow YOUR_SOLUTION_HERE)



;;; Now you have many apples (5) instead of one.
;;; Function the same as previous but it takes set of apples instead of the single apple.
;;; Each apple in the set is a vector of x and y.
;;; E.g. you can try to reach nearest apple to the snake.

;;; Uncomment and substitute your solution
; (run-many-apples YOUR_SOLUTION_HERE)



;;; Walls are added. So snake can hit wall and die.
;;; Your function now takes third argument - set of walls.
;;; Each wall is a cell that snake is not allowed to  move to.
;;; Wall is a vector of x and y.

;;; Uncomment and substitute your solution
; (run-with-walls YOUR_SOLUTION_HERE)