(ns publicator.domain.abstractions.aggregate
  (:refer-clojure :exclude [assert])
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Aggregate
  (spec [this] "Aggregate validation spec"))

(defn assert [agg]
  (s/assert (spec agg) agg))

(defn nilable-assert [agg]
  (s/assert (s/nilable (spec agg)) agg))
