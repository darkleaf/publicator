(ns publicator.domain.abstractions.instant-impl
  (:require
   [publicator.domain.abstractions.instant :as instant])
  (:import
   [java.time Instant]))

(defn binding-map []
  {#'instant/now (fn [] (Instant/now))})
