(ns k-means.work
  (:require [k-means.core :refer [run-empty run-2-clusters run-3-clusters run-random-clusters]]))


;;; Your task is to implement clustering algorithm.
;;; You're a given a set of points on plan and goal is to divide them into k clusters.
;;; Implement k-means algorithm to solve this task: http://en.wikipedia.org/wiki/K-means_clustering
;;; Your function must take collection of points. Each point is [x y].
;;; It must return collection of clusters. Each cluster - collection of points.
;;; Example: there are 4 points: [0 0] [1 1] [9 9] [10 10] and you need to partition them into 2 clusters.
;;; Input will be [[0 0] [9 9] [1 1] [10 10]] and output should be something like [[[0 0] [1 1]] [[9 9] [10 10]]].
;;; Note that you don't get k - number of clusters. You need to specify it somewhere in function.
;;; To test you solution use following tests:

; (run-empty SOLUTION)

; (run-2-clusters SOLUTION)

; (run-3-clusters SOLUTION)

;;; Interaction: mouse click - add new point
;;;              space - reset simulation (remove all points or regenerate them, depenends on test)
;;; Note: you may need use different version of your function (with k = 2 for run-2-clusters and  k = 3 for run-3-clusters).



;;; Now try to improve your solution so it can determine k based on given set of points. So if there are visually 3 clusters it should partition points to 3 clusters, if 4 than to 4 clusters.
;;; Test your solution on this test:

; (run-random-clusters SOLUTION)



;;; Implement some other clustering algorithm.
