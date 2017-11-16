(ns publicator.impl.hasher
  (:require
   [buddy.hashers]
   [publicator.domain.abstractions.hasher :as hasher]))

(deftype Hasher []
  hasher/Hasher
  (-derive [_ password]
    (buddy.hashers/derive password))
  (-check [_ attempt encrypted]
    (buddy.hashers/check attempt encrypted)))

(defn binding-map []
  {#'hasher/*hasher* (Hasher.)})
