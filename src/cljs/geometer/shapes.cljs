(ns geometer.shapes
  (:require [thi.ng.geom.core       :as g]
            [thi.ng.geom.circle     :refer [circle]]
            [thi.ng.geom.aabb       :refer [aabb]]
            [thi.ng.geom.sphere     :as s]))

(defn cube
  "Return the vertices with which to draw a tesselated cube."
  []
  (-> (aabb 25)   ;; a cuboid of 25 units size
      (g/as-mesh))) ;; as a mesh

(defn disc
  "Return the vertices with which to draw a tesselated disc."
  []
  (-> (circle 16)    ;; a 2D circle of 16 units
      (g/extrude {}) ;; extruded into a 3D disc
      (g/as-mesh)))  ;; as a mesh

(defn sphere
  "Return the vertices with which to draw a tesselated sphere."
  []
  (-> (s/sphere 16) ;; abstract sphere of 16 units
      (g/as-mesh))) ;; as a mesh
