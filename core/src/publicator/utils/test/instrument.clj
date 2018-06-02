(ns publicator.utils.test.instrument
  (:require
   [orchestra.spec.test :as st]))

(defn fixture [f]
  (locking st/instrument
    (st/instrument))
  (f))
