(ns publicator.domain.abstractions.hasher
  (:refer-clojure :exclude [derive]))

(defprotocol Hasher
  (-derive [this password])
  (-check [this attempt encrypted]))

(declare ^:dynamic *hasher*)

(defn derive [password]
  (-derive *hasher* password))

(defn check [attempt encrypted]
  (-check *hasher* attempt encrypted))
