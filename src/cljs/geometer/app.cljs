(ns geometer.app
  (:require [thi.ng.geom.core                :as g]
            [thi.ng.geom.core.vector         :as v :refer [vec2 vec3]]
            [thi.ng.geom.core.matrix         :as mat :refer [M44]]
            [thi.ng.geom.core.utils          :as gu]
            [thi.ng.geom.rect                :refer [rect]]
            [thi.ng.geom.webgl.core          :as gl]
            [thi.ng.geom.webgl.animator      :refer [animate]]
            [thi.ng.geom.webgl.buffers       :as buf]
            [thi.ng.geom.webgl.shaders       :as sh]
            [thi.ng.geom.webgl.shaders.basic :as basic]
            [thi.ng.geom.webgl.shaders.phong :as phong]
            [thi.ng.math.core                :as m]
            [thi.ng.typedarrays.core         :refer [float32]]
            [thi.ng.geom.polygon :as poly]
            [thi.ng.geom.basicmesh :refer [basic-mesh]]
            [geometer.shapes  :as shapes]
            [geometer.lsystem :as lsystem]
            [geometer.genetic :as genetic]))

(enable-console-print!)

;; we use defonce for the webgl context, mouse tracking atoms and
;; model they won't be re-initialed when the namespace is reloaded
(defonce gl (gl/gl-context "main"))
(defonce view-rect (atom nil))

(defonce mouse-x (atom 0))
(defonce mouse-y (atom 0))

(defonce projection (atom nil))
(defonce model (atom nil))

(defonce viewpoint (atom (g/translate M44 0 0 -70)))
(defonce eye-separation (atom 0))
(defonce render-mode (atom :normal))

(defonce shader (sh/make-shader-from-spec gl phong/shader-spec))

(defn- set-model!
  "Our model is a `mesh` that will be rendered by the animation loop started in the start function."
  [mesh]
  (reset! model
          (-> (g/center mesh)
              (gl/as-webgl-buffer-spec {})
              (buf/make-attribute-buffers-in-spec gl gl/static-draw)
              (assoc :shader shader)
              (update-in [:uniforms] merge
                         {:proj          @projection
                          :lightPos      (vec3 2 0 5)
                          :ambientCol    0x181818
                          :diffuseCol    0x10a04c
                          :specularCol   0xaaaaaa
                          :shininess     100
                          :wrap          1
                          :useBlinnPhong true}))))

(defn ^:export new-model
  "Selects a new model from a set of possibilities (or a cube if we don't recognise the request). This requires some setTimeout silliness for the browser to show a status panel."
  [kind]
  (let [status (.getElementById js/document "status")]
    (set! (.-innerHTML status) (str "Generating new " kind " model..."))
    (set! (.-className status) "visible")
    (js/setTimeout
     #(do
        (case kind
          "algae"   (set-model! (lsystem/algae))
          "koch"    (set-model! (lsystem/koch))
          "toroid"  (set-model! (lsystem/toroid))
          "novelty" (set-model! (genetic/novelty-search))
          "disc"    (set-model! (shapes/disc))
          "sphere"  (set-model! (shapes/sphere))
          (set-model! (shapes/cube)))
        (set! (.-className status) "invisible"))
     20)))

(defn- hud-message
  "Display message in HUD overlay."
  [body]
  (set! (.-innerHTML (.getElementById js/document "hud")) body))

(defn ^:export set-render-mode
  [id]
  (reset! render-mode (keyword id))
  (hud-message
   (when (= :stereo @render-mode)
     (str "eye separation: " @eye-separation))))

(defn- keypress-handler [e]
  (let [k (.-keyCode e)]
    (case k
      119 (swap! viewpoint g/translate 0 0 2)  ;; w = forward
      115 (swap! viewpoint g/translate 0 0 -2) ;; s = backward
      91  (swap! eye-separation - 0.01)        ;; [ = -eye separation
      93  (swap! eye-separation + 0.01)        ;; ] = +eye separation
      (print k))
    (when (= :stereo @render-mode)
      (hud-message (str "eye separation: " @eye-separation)))))

(defn resize-handler
  "Window resize handler. Resizes canvas, updates view rects and
  projection matrices."
  []
  (let [w      (.-innerWidth js/window)
        h      (.-innerHeight js/window)
        canv   (.getElementById js/document "main")
        w2     (/ w 2)
        view   (rect 0 0 w h)
        view-l (rect 0 0 w2 h)
        view-r (rect w2 0 w2 h)]
    (set! (.-width canv) w)
    (set! (.-height canv) h)
    (reset! view-rect
            {:normal view
             :left   view-l
             :right  view-r})
    (reset! projection
            {:normal (gl/perspective 45 view 0.1 200.0)
             :left   (gl/perspective 45 view-l 0.1 200.0)
             :right  (gl/perspective 45 view-l 0.1 200.0)})))

(defn- update-pos [e]
  (reset! mouse-x (* 0.01 (- (.-clientX e) (/ (.-innerWidth js/window) 2))))
  (reset! mouse-y (* 0.01 (- (.-clientY e) (/ (.-innerHeight js/window) 2)))))

(defn- draw-model-in-view
  "Takes a semi-complete model spec map, view id and eye separation
  offset. Sets up and clears viewport, enables scissor test and draws
  mesh with injected projection and view matrices for requested view."
  [model id eye-sep]
  (let [{[x y] :p [w h] :size :as view} (@view-rect id)]
    (gl/set-viewport gl view)
    (gl/enable gl gl/scissor-test)
    (.scissor gl x y w h) ;; TODO add wrapper in geom.webgl.core
    (gl/clear-color-buffer gl 0 0 0 0) ;; 0 opacity, so we see the bg gradient
    (gl/clear-depth-buffer gl 1)
    (gl/enable gl gl/depth-test)
    (phong/draw
     gl (update model :uniforms merge
                {:proj  (@projection id)
                 :view  (mat/look-at (vec3 eye-sep 0 2) (vec3) (vec3 0 1 0))}))
    (gl/disable gl gl/scissor-test)))

(defn ^:export start
  "This function is called when 'index.html' loads. We use it to kick off mouse tracking, a keyboard handler and the animation loop."
  []
  (.addEventListener js/document "keypress" keypress-handler)
  (.addEventListener js/document "mousemove" update-pos)
  (.addEventListener js/window "touchmove"
                     #(do (.preventDefault %)
                          (update-pos (aget (.-touches %) 0))))
  (.addEventListener js/window "resize" resize-handler)
  (resize-handler)

  ;; initialize with a cube
  (set-model! (shapes/cube))

  (animate
   (fn [[t frame]]
     (let [m (update @model :uniforms merge
                     {:model (-> @viewpoint
                                 (g/translate 0 0 0)
                                 (g/rotate-x @mouse-y)
                                 (g/rotate-y @mouse-x))})]
       (if (= :normal @render-mode)
         (draw-model-in-view m :normal 0)
         (do
           (draw-model-in-view m :left (- @eye-separation))
           (draw-model-in-view m :right (+ @eye-separation))))
       true))))
