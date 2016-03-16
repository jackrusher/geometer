(ns geometer.csg
  (:require [thi.ng.geom.core             :as g]
            [thi.ng.geom.cuboid           :refer [cuboid]]
            [thi.ng.geom.gmesh            :as gm]
            [thi.ng.geom.mesh.csg         :as csg]
            [thi.ng.geom.mesh.subdivision :as sd]
            [thi.ng.math.core             :as m]))

(defn starfighter []
  ;; need under cljs to prevent a stack overflow related to floating
  ;; point precision
  (binding [thi.ng.math.core/*eps* 1e-4] 
    (let [prototype (-> (cuboid [0.0 0.0 0.0]
                                [0.0 0.0 3.0]
                                [40.0 0.0 10.0]
                                [40.0 0.0 0.0]
                                [0.0 40.0 0.0]
                                [0.0 40.0 3.0]
                                [40.0 40.0 10.0]
                                [40.0 40.0 0.0])
                        (g/center))
          [back-box top-wing bottom-wing cutout bbox]
          (map (comp csg/mesh->csg
                     g/tessellate
                     sd/catmull-clark
                     #(g/as-mesh % {:mesh (gm/gmesh)}))
               [prototype
                (-> prototype (g/rotate-z (m/radians -65)) (g/center) (g/translate [-5 -20 0]) )
                (-> prototype (g/rotate-z (m/radians 65)) (g/center) (g/translate [-5 20 0]) )
                (-> (cuboid 60) (g/center) (g/translate [-58 0 0]))
                (-> (cuboid 30) (g/center) (g/translate [-25 0 0]))])]
      (-> back-box
          (csg/union top-wing)
          (csg/union bottom-wing)
          (csg/subtract cutout)
          (csg/intersect bbox)
          csg/csg->mesh))))
