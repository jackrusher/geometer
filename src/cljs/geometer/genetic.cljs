(ns geometer.genetic
  (:require [thi.ng.geom.core :as g]
            [thi.ng.math.core :as m]
            [geometer.turtle  :as t]
            [clojure.data     :refer [diff]]))

(defn random-gene
  "A function to return a random operation to produce variation in each generation. Repeating an operation in the vector of ops increases its likelihood."
  []
  (rand-nth [t/line t/line t/line t/line t/rx t/rx- t/ry t/ry- t/rz t/rz-]))
;;  t/center

(defn mesh-distance
  "An extremely primitive function to calculate the difference between mesh vertices `old` and `new`. Used as a fitness function for a genetic algorithm that follows."
  [old new]
  (apply + (map #(apply min (map (partial g/dist %) old)) new)))

(defn novelty-search
  "Use a novelty search genetic algorithm to produce the most 'interesting' mesh possible given these constraints."
  []
  (let [generation-size 8
        genes-per-generation 18]
    (loop [generations 10
           state t/fresh-turtle]
      (if (zero? generations)
        (:mesh state)
        (recur (dec generations)
               (->> (repeatedly generation-size
                                #(repeatedly genes-per-generation random-gene))
                    (map (partial t/turtle-mesh state))
                    (map (fn [kid]
                           (vector (mesh-distance (g/vertices kid)
                                                  (g/vertices (:mesh state)))
                                   (assoc state :mesh kid))))
                    (apply min-key first)
                    second))))))
