(ns ^:figwheel-always
  netzwaechterlein.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [netzwaechterlein.server-test]))

(doo-tests 'netzwaechterlein.server-test)
