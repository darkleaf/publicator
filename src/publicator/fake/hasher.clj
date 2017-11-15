(ns publicator.fake.hasher
  (:require
   [publicator.domain.abstractions.hasher :as hasher]))

(deftype Hasher []
  hasher/Hasher

  (-derive [_ password]
    password)

  (-check [_ attempt encrypted]
    (= attempt encrypted)))

(defn binding-map []
  {#'hasher/*hasher* (->Hasher)})
