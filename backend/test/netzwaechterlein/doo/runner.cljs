(ns netzwaechterlein.doo.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [netzwaechterlein.core-test]
            [netzwaechterlein.db-test]
            [netzwaechterlein.sensors-test]))

(doo-tests 'netzwaechterlein.core-test
           'netzwaechterlein.db-test
           'netzwaechterlein.sensors-test)
