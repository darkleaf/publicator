(ns publicator.domain.abstractions.test-impl.occupation-stub
  (:require
   [publicator.domain.abstractions.occupation :as occupation]))

(defn binding-map []
  {#'occupation/*occupied* (constantly false)})
