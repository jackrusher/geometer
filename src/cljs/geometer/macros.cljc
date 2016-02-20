(ns geometer.macros
  (:require [thi.ng.geom.core :as g]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core.vector :refer [vec3]]
            [thi.ng.geom.core.matrix :refer [M44]]
            [thi.ng.geom.aabb        :refer [aabb]]))

(defmacro defmove
  "Creates a new movement operation called `op` that handles bookkeeping for :last-length and provides `length` to the lexical environment of the code passed in, which should be three expressions for the change in `x`, `y` and `z`."
  [op x y z]
  `(defn ~op [t#]
     (let [~'length ((:length-fn t#) t#)]
       (update (assoc t# :last-length ~'length) :matrix g/translate ~x ~y ~z))))

(defmacro defrot
  "Creates a new rotation operation called `op` on `axis` that handles bookkeeping for :last-angle and provides `angle` to the lexical environment of the rotation `expr`."
  [op axis expr]
  (let [rot-fn (case axis
                 x 'thi.ng.geom.core/rotate-x
                 y 'thi.ng.geom.core/rotate-y
                 z 'thi.ng.geom.core/rotate-z)]
    `(defn ~op [t#]
       (let [~'angle ((:angle-fn t#) t#)]
         (update (assoc t# :last-angle ~'angle)
                 :matrix ~rot-fn (m/radians ~expr))))))

(defmacro defshape [name body]
  "Creates a new shape primtive called `name` that handles turtle matrix updates, bookkeeping for :last-length and provides `length` to the lexical environment of `body`."
  `(defn ~name [t#]
     (let [~'length ((:length-fn t#) t#)]
       (assoc t#
              :last-length ~'length
              :matrix (g/translate (:matrix t#) 0 0 ~'length)
              :mesh   (g/into (:mesh t#)
                              (-> ~body
                                  (g/transform (:matrix t#))
                                  (g/as-mesh)))))))
