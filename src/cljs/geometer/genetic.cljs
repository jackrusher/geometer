(ns geometer.genetic
  (:require [thi.ng.geom.core :as g]
            [thi.ng.math.core :as m]
            [geometer.turtle  :as t]
            [clojure.data     :refer [diff]]))

;; (defn random-gene
;;   "A function to return a random operation to produce variation in each generation. Repeating an operation in the vector of ops increases its likelihood."
;;   []
;;   (rand-nth [\F \F \F \C \+ \- \& \^ \\ \/ \| \[ \]]))

;; (defn mesh-distance
;;   "An extremely primitive function to calculate the difference between mesh vertices `old` and `new`. Used as a fitness function for a genetic algorithm that follows."
;;   [old new]
;;   (apply + (map #(apply min (map (partial g/dist %) old)) new)))

(defn novelty-search
  "Use a novelty search genetic algorithm to produce the most 'interesting' mesh possible given these constraints."
  []
  ;; (let [generation-size 8
  ;;       genes-per-generation 18]
  ;;   (loop [generations 10
  ;;          state ls/initial-state]
  ;;     (if (zero? generations)
  ;;       (last state)
  ;;       (recur (dec generations)
  ;;              (->> (repeatedly generation-size #(repeatedly genes-per-generation random-gene))
  ;;                   (map (partial ls/execute-ops
  ;;                                 (constantly (m/radians 90))
  ;;                                 (partial ls/grow-cuboid 4)
  ;;                                 state))
  ;;                   (map (fn [kid] (vector (mesh-distance (g/vertices (last kid))
  ;;                                                        (g/vertices (last state)))
  ;;                                         kid)))
  ;;                   (apply min-key first)
  ;;                   second)))))
  )

;;
;; TODO
;;
;; (defn evolve-toroid
;;   "Use a genetic algorithm to search for toroid-like shapes."
;;   []
;;   (let [generation-size 10
;;         genes-per-generation 6]
;;     (loop [generations 20
;;            state [[(vec3 0 0 0)] [(vec3 0 0 0)] (gm/gmesh)]]
;;       (.log js/console (pr-str (mesh-distance toroid (g/vertices (last state)))))
;;       (if (or (zero? generations))
;;         (last state)
;;         (recur (dec generations)
;;                (->> (repeatedly generation-size #(repeatedly genes-per-generation random-gene))
;;                     (map (fn [ops] (execute-ops state ops #(m/radians 15) #(identity 4))))
;;                     (map #(vector (mesh-distance toroid (g/vertices (last %))) %))
;;                     (apply min-key first)
;;                     second))))))
