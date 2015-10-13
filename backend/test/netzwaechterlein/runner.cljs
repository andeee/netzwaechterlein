(ns ^:figwheel-always
  netzwaechterlein.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [netzwaechterlein.core-test]
            [netzwaechterlein.db-test]
            [netzwaechterlein.sensors-test]
            [netzwaechterlein.websocket-test]
            [netzwaechterlein.server-test]))

(run-tests 'netzwaechterlein.core-test
           'netzwaechterlein.db-test
           'netzwaechterlein.sensors-test
           'netzwaechterlein.websocket-test
           'netzwaechterlein.server-test)
