(ns publicator.domain.aggregate
  (:require
   [clojure.spec.alpha :as s]))

(defmulti spec class)

(s/def ::spec (s/multi-spec spec class))
(s/def ::nilable-spec (s/nilable ::spec))
