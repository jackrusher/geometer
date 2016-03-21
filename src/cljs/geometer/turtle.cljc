(ns geometer.turtle
  (:require [thi.ng.geom.core      :as g]
            [thi.ng.geom.aabb      :refer [aabb]]
            [thi.ng.geom.basicmesh :as bm]
            [thi.ng.geom.circle    :refer [circle]]
            [thi.ng.geom.matrix    :refer [M44]]
            [thi.ng.geom.sphere    :refer [sphere]]
            [thi.ng.geom.vector    :refer [vec3]]
            [thi.ng.math.core      :as m])
  #?(:clj (:require [geometer.macros :refer [defmove defrot defshape]]))
  #?(:cljs (:require-macros [geometer.macros :refer [defmove defrot defshape]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MOVEMENT/ROTATION

(defn center [turtle]
  (assoc turtle :matrix M44))

(defmove tx  length 0 0)
(defmove ty  0 length 0)
(defmove tz  0 0 length)
(defmove tx- (- length) 0 0)
(defmove ty- 0 (- length) 0)
(defmove tz- 0 0 (- length))

(defrot rx  x angle)
(defrot ry  y angle)
(defrot rz  z angle)
(defrot rx- x (- angle))
(defrot ry- y (- angle))
(defrot rz- z (- angle))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SHAPE PRIMITIVES

(defshape line
  (aabb (vec3 -0.25 -0.25 0) (vec3 0.5 0.5 length)))

(defshape box
  (aabb (vec3 (- (/ length 2)) (- (/ length 2)) 0) (vec3 length length length)))

(defshape plane
  (aabb (vec3 (- (/ length 2)) -0.25 0) (vec3 length 0.5 length)))

(defshape ball
  (g/as-mesh (sphere length)))

(defshape cylinder
  (g/into (-> (circle 1) (g/center) (g/extrude {:depth length}))
          (-> (sphere 1) (g/center) (g/as-mesh)))) ; ball end

(defshape disc
 (g/extrude (circle length) {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TURTLE PARAMETERS

(defn set-attr-fn
  "Returns a function that sets the `attr` function in the turtle to `x`, or if `x` is a number, to `(constantly x)`."
  [attr x]
  #(assoc %1 attr (if (number? x) (constantly x) x)))

(defn angle [a] (set-attr-fn :angle-fn a))
(defn length [l] (set-attr-fn :length-fn l))
(defn thickness [t] (set-attr-fn :thickness-fn t))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GAMERA

;; TODO add max-depth, max-objects, maybe seed?
(def fresh-turtle
  {:matrix       M44
   :mesh         (bm/basic-mesh)
   :angle-fn     (constantly 90)
   :length-fn    (constantly 1)
   :thickness-fn (constantly 0.5)
   :depth        1})

(defn turtle-mesh
  "Execute a sequence of turtle instructions, return the resulting mesh."
  ([steps] (turtle-mesh fresh-turtle steps))
  ([turtle steps]
   (:mesh (reduce
           (fn [t step]
             (if (sequential? step) ; push matrix, do contained steps
               (update t :mesh #(g/into % (turtle-mesh t step)))
               (step t)))
           (update turtle :depth inc)
           steps))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; some examples
;; TODO move examples to another file later

(defn hoops
  "Generates a group of four toroids in a mesh using 3D turtle graphics primitives."
  []
  (turtle-mesh [(length 3)
                (angle 15)
                (map #(take 48 (cycle [line %])) [rx- rx ry ry-])]))

(defn hexen
  "A structure made of nesting hexagons."
  []
  (turtle-mesh
   (map (fn [a turn-out turn len]
          (concat
           [(angle a) turn-out (length (/ len 2)) tz (length len) (angle 60)]
           (take 12 (cycle [turn line]))))
        [60 120 240 300 0 45 90 -90 -45]
        (cycle [rx- rx ry- ry])
        (cycle [ry- ry rx rx-])
        [6 6 6 6 9 9 9 9])))

;; TODO add color to the "flower" ends
;; BUG very slow with latest geom, can no longer go more than one deep
(defn branch
  "A recursive branching tree structure with half-cylinder flowers."
  [level]
  (if (>= 0 level)
    [(length 1) cylinder]
    [(angle #(m/random 3 5)) ry-
     (length #(* (or (:last-length %) 2.5) (m/random 0.75 1.5)))
     (map (fn [[a b]] (concat (take 8 (cycle [a b])) (branch (dec level))))     
          [[ry line] [ry- line] [rx line] [rx- line]])]))

(defn plant []
  (turtle-mesh (branch 1)))
