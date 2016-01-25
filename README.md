![splash](https://raw.githubusercontent.com/jackrusher/geometer/master/resources/images/geometer.png)

# Geometer

In my continuing effort to get the [Clojure](http://clojure.org)
community to notice [Karsten Schmidt](http://postspectacular.com), and
the creative coding community to notice Clojure, I've created this
little interactive playground for experiments with Karsten's
[th.ing](https://github.com/thi-ng) libraries.

## Getting Started

After checking out the repo (and assuming
[boot](https://github.com/boot-clj/boot) is installed):

1. Start the `boot` development server: `$ boot dev`

2. Point your browser of choice at `http://localhost:3000/`

3. Connect your editor to the `nrepl` server that `boot` started,
   which in `emacs` can done via `M-x cider-connect` ⇒ `localhost` ⇒
   `geometer:port-number`.

4. Refresh your browser to make sure all the bits are talking to each other.

5. If you're using `emacs`, `cider-eval-buffer`.

6. Begin evaluating forms.

N.B. Saving any `cljs` file will trigger recompilation of the entire file
and an automatic reload of the namespace. The current model and view
rotation are defined using `defonce` to avoid jankiness.

## The Code

There are examples of creating meshes from primitive shapes, including
extruding 3D meshes from 2D primitives, and implementations for a
couple of simple generative techniques. More will follow.
