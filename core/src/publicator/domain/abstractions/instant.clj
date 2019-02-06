(ns publicator.domain.abstractions.instant)

(defprotocol Instant
  (-now [this]))

(declare ^:dynamic *instant*)

(defn now []
  (-now *instant*))
