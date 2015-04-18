(ns many-worlds.api
  (:require [clojure.edn :as edn]
            [compojure.core :refer [GET PUT routes]]
            [compojure.route :as route]
            [quil.core :as quil]
            [quil.applet :refer [*applet*]]
            [qutils.util :refer [restore-state]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :as resp])
  (:import [java.awt.image BufferedImage]
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [javax.imageio ImageIO]))

(defn parse-frame-opts
  ([opts] (parse-frame-opts opts nil))
  ([opts state]
   (let [{t-str :t, w-str :width, h-str :height} opts
         t (let [t (edn/read-string (or t-str "latest"))]
             (if (number? t)
               t
               (or (-> state :path keys last) 0)))
         read-dim (fn [string]
                    (let [value (edn/read-string (or string "-1"))]
                      (if (and (number? value) (pos? value))
                        value
                        nil)))]
     {:t t, :width (read-dim w-str), :height (read-dim h-str)})))

(defn ^BufferedImage render-frame
  "Given a quil sketch, the default width and height, the sketch's configure
  and draw functions, and the `t` (time) and `s` (scale) at which to render sketch,
  return a rendering of the sketch subject to `t` and `s` as a BufferedImage."
  [sketch w h configure! draw! t s]
  (binding [*applet* sketch]
    (let [w' (int (* w s))
          h' (int (* h s))
          g (quil/create-graphics w' h' :java2d)]
      (quil/with-graphics g
        (configure!)
        (quil/scale (double s))
        (draw! t))
      (.getImage g))))

(defn handler
  "Given a state atom, a var containing a quil sketch, nominal sketch width and
  height, and configure and draw functions for the sketch, define an API handler
  that serves rendered images of the sketch, the state atom's current value, and
  can reset the state atom with a submitted state value.

  The `draw!` function must be a function of `t`. Any other state it wishes to
  rely on it must handle itself; if only the current position of the bezier walk
  is needed, then `t` alone will suffice.

  The `configure!` function should *not* be the sketch's setup function, since it
  will be called every time that a frame is rendered. The purpose of the
  `configure!` function is to set any global quil sketch options such as
  smoothing or color mode. There is no guarantee that any of these global sketch
  settings will be persisted between renderings.

  Here's a detailed breakdown of the routes that the handler serves:

    - GET '/state'
      Return the EDN-serialized contents of the state atom. Does not accept any
      query parameters.

    - PUT '/state'
      Replace the sketch's state atom with the state submitted in the request body.
      The request body must be in EDN format. Does not accept any query parameters.
      Returns a 302 redirect to 'frame.png?t=latest'.

    - GET '/frame.png'
      Return a PNG rendering of the sketch by calling the `configure!` and
      `draw!` functions. Accepts three query parameters: `t`, `width` and
      `height`. The size of the frame may be defined by specifying either the
      `width` or `height` in pixels. The returned image will have the specified
      length in that dimension, with the other dimension scaled as appropriate
      to preserve the sketch's aspect ratio. If both are specified, only the
      `width` value is used, and the height is again derived using the aspect
      ratio. The time of the frame is governed by the `t` query parameter. If
      it's a number that number is used directly; if it's not a number, or it's
      not provided, then the time corresponding to the start of the last
      generated bezier segment of the random walk is used."
  [!state sketch-var w h configure! draw!]
  (-> (routes
        (GET "/state" [] (pr-str @!state))

        (PUT "/state" [state-str]
             (restore-state !state state-str)
             (resp/redirect "frame.png?t=latest"))

        (GET "/frame.png" [t width height]
             (let [frame-opts {:t t, :width width, :height height}
                   {:keys [t width height]} (parse-frame-opts frame-opts @!state)
                   scale (cond
                           width (/ width (* w 1.0))
                           height (/ height (* h 1.0))
                           :else 1)
                   image (render-frame @sketch-var w h configure! draw! t scale)
                   stream (new ByteArrayOutputStream)]
               (ImageIO/write image "PNG" stream)
               {:status 200
                :headers {"Content-Type" "image/png"}
                :body (ByteArrayInputStream. (.toByteArray stream))}))

        ;; console application
        (GET "/" [] (resp/resource-response "public/index.html"))
        (route/resources ""))

    wrap-keyword-params
    wrap-params))
