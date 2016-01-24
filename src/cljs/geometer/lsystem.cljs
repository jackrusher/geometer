(ns geometer.lsystem
  (:require [thi.ng.geom.core        :as g]
            [thi.ng.geom.core.utils  :as gu]
            [thi.ng.geom.core.vector :as v :refer [vec2 vec3]]
            [thi.ng.geom.cuboid      :refer [cuboid]]
            [thi.ng.geom.gmesh       :as gm]
            [thi.ng.geom.types.utils :as tu]
            [thi.ng.math.core        :as m]))

(defn grow [pos rot mesh len]
  (let [[x y z] (last rot)
        corners (map #(-> % (g/rotate-x x) (g/rotate-y y) (g/rotate-z z) (g/+ (last pos)))
                     [(vec3 0 0 0) (vec3 -1 0 0) (vec3 0 -1 0) (vec3 0 1 0)
                      (vec3 -1 0 len) (vec3 0 -1 len) (vec3 0 1 len) (vec3 0 0 len)])]
    [(conj (pop pos) (last corners))
     rot
     (tu/into-mesh mesh gm/add-face (apply cuboid corners))]))

(defn- execute-op* [[pos rot mesh] op angle len]
  (case op
    \F (grow pos rot mesh (len))
    \C [(conj (pop pos) (vec3 0 0 0)) (conj (pop rot) (vec3 0 0 0)) mesh]
    \+ [pos (conj (pop rot) (g/+ (last rot) (vec3 0 (angle) 0))) mesh]
    \- [pos (conj (pop rot) (g/+ (last rot) (vec3 0 (- (angle)) 0))) mesh]
    \& [pos (conj (pop rot) (g/+ (last rot) (vec3 (angle) 0 0))) mesh]
    \^ [pos (conj (pop rot) (g/+ (last rot) (vec3 (- (angle)) 0 0))) mesh]
    \\ [pos (conj (pop rot) (g/+ (last rot) (vec3 0 0 (angle)))) mesh]
    \/ [pos (conj (pop rot) (g/+ (last rot) (vec3 0 0 (- (angle))))) mesh]
    \| [pos (conj (pop rot) (g/+ (last rot) (vec3 0 (m/radians 180) 0))) mesh]
    \[ [(conj pos (last pos)) (conj rot (last rot)) mesh]
    \] (if (or (= 1 (count pos)) (= 1 (count rot)))
         [pos rot mesh]
         [(pop pos) (pop rot) mesh]) 
    [pos rot mesh]))

(defn execute-ops [state ops angle len]
  (reduce #(execute-op* %1 %2 angle len) state ops))

(defn expand-l-system [rules curr-state depth]
  (if (zero? depth)
    curr-state 
    (mapcat #(expand-l-system rules (rules % [%]) (dec depth)) curr-state)))

(defn build-lsystem [l-system angle len]
  (last (execute-ops [[(vec3 0 0 0)] [(vec3 0 0 0)] (gm/gmesh)] l-system angle len)))

(defn algae []
  (build-lsystem (expand-l-system {\F "F[&&+F]F[-\\F][-\\^F][&F][&+F]"} "F" 3)
                                #(m/radians (+ (m/random -5 5) 35))
                                #(+ (m/random 2 4))))

(defn koch []
  (build-lsystem (expand-l-system {\F "FF-F-F&F-F-F+F"} "F" 3)
                 #(m/radians 90)
                 #(identity 2)))
