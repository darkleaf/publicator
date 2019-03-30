(ns publicator.domain.abstractions.test-impl.uniqueness-stub
  (:require
   [publicator.domain.abstractions.uniqueness :refer [*is-unique*]]))

(defn binding-map []
  {#'*is-unique* (constantly true)})
