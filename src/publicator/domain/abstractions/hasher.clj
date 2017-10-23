(ns publicator.domain.abstractions.hasher
  (:refer-clojure :exclude [derive])
  (:require [clojure.spec.alpha :as s]))

(defprotocol Hasher
  (-derive [this password])
  (-check [this attempt encrypted]))

(declare ^:dynamic *hasher*)

(s/def ::encrypted string?)

(defn derive [password]
  (s/assert ::encrypted
            (-derive *hasher* password)))

(defn check [attempt encrypted]
  (s/assert ::encrypted encrypted)
  (-check *hasher* attempt encrypted))
