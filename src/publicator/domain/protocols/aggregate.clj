(ns publicator.domain.protocols.aggregate
  (:refer-clojure :exclude [assert])
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Aggregate
  (spec [this] "Aggregate validation spec"))

(defn assert [agg]
  {:pre [(satisfies? Aggregate agg)]}
  (s/assert (spec agg) agg))

(defn nilable-assert [agg]
  (when agg (assert agg))
  agg)
