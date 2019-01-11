(ns publicator-ext.domain.abstractions.instant
  (:require
   [clojure.spec.alpha :as s])
  (:import
   [java.time Instant]))

(defn ^:dynamic *now* []
  (Instant/now))

(s/fdef now
  :ret inst?)

(defn now []
  (*now*))
