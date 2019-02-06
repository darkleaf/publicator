(ns publicator-ext.domain.abstractions.instant-impl
  (:require
   [publicator-ext.domain.abstractions.instant :as instant])
  (:import
   [java.time Instant]))

(deftype InstantImpl []
  instant/Instant
  (-now [this]
    (Instant/now)))

(defn binding-map []
  {#'instant/*instant* (InstantImpl.)})
