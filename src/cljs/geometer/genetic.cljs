(ns geometer.genetic
  (:require [thi.ng.math.core        :as m]
            [thi.ng.geom.core        :as g]
            [thi.ng.geom.core.vector :refer [vec3]]
            [thi.ng.geom.gmesh       :as gm]
            [geometer.lsystem        :refer [grow execute-ops]]
            [clojure.data            :refer [diff]]))

(defn random-gene
  "A function to return a random operation to produce variation in each generation. Repeating an operation in the vector of ops increases its likelihood."
  []
  (rand-nth [\F \F \F \C \+ \- \& \^ \\ \/ \| \[ \]]))

(defn evolve-mesh
  "Use novelty search to produce the most 'interesting' mesh possible given these constraints."
  []
  (let [generation-size 5
        genes-per-generation 25]
    (loop [generations 5
           state [[(vec3 0 0 0)] [(vec3 0 0 0)] (gm/gmesh)]]
      (if (zero? generations)
        (last state)
        (recur (dec generations)
               (->> (repeatedly generation-size
                                #(repeatedly genes-per-generation random-gene))
                    (map (fn [ops]
                           (execute-ops state ops
                                        #(m/radians (rand-nth [0 90 180 270]))
                                        #(m/random 4 8))))
                    (map #(vector (diff (last %) (last state)) %))
                    (apply max-key first)
                    second))))))
