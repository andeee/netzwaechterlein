(ns ^:figwheel-always
  netzwaechterlein.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [netzwaechterlein.server-test]))

(run-tests 'netzwaechterlein.server-test)
