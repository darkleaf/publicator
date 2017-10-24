(ns publicator.domain.abstractions.aggregate
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Aggregate
  (spec [this] "Aggregate validation spec"))
