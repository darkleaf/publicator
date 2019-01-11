(ns publicator-ext.utils.test.instrument
  (:require
   [orchestra.spec.test :as st]))

(defn fixture [f]
  (st/instrument)
  (f))
