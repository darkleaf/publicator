(ns publicator.utils.test.instrument
  (:require
   [robert.hooke :as hooke]
   [orchestra.spec.test :as st]
   [clojure.test :as t]))

(defn- instrument [f & args]
  (st/instrument)
  (apply f args))

(hooke/add-hook #'t/run-tests #'instrument)
