(ns ^:figwheel-always
  netzwaechterlein.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [netzwaechterlein.core-test]
            [netzwaechterlein.db-test]
            [netzwaechterlein.sensors-test]))

(run-tests 'netzwaechterlein.core-test
           'netzwaechterlein.db-test
           'netzwaechterlein.sensors-test)
