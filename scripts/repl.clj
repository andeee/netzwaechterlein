(require
 '[cemerick.piggieback :as repl]
 '[cljs.repl.node :as node])

(repl/cljs-repl (node/repl-env))
