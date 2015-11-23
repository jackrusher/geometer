# geometer

An interactive playground for experiments with th.ing's geom libraries.

After checking out the repo (and assuming [boot](https://github.com/boot-clj/boot) is installed):

1. Start the `boot` development server: `$ boot dev`

2. Point your browser of choice at `http://localhost:3000/`

3. Connect your editor to the `nrepl` server that `boot` started,
   which in `emacs` can done via `M-x cider-connect` ⇒ `localhost` ⇒
   `geometer:port-number`.

4. Refresh your browser to make sure all the bits are talking to each other.

5. If you're using `emacs`, `cider-eval-buffer`.

6. Begin evaluating forms, perhaps tinkering with the model definition
   in `app.cljs` to change the shape/colors projected on the WebGL
   canvas.

N.B. Saving `app.cljs` will trigger recompilation of the entire file
and an automatic reload of the namespace.
