(ns publicator.domain.abstractions.aggregate
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Aggregate
  (id [this] "Aggregate id")
  (spec [this] "Aggregate validation spec"))
