(ns netzwaechterlein.doo.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [netzwaechterlein.server-test]))

(doo-tests 'netzwaechterlein.server-test)
