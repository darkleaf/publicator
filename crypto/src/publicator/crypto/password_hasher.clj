(ns publicator.crypto.password-hasher
  (:require
   [buddy.hashers]
   [publicator.domain.abstractions.password-hasher :as password-hasher]))

(deftype PasswordHasher []
  password-hasher/PasswordHasher
  (-derive [_ password]
    (buddy.hashers/derive password))
  (-check [_ attempt encrypted]
    (buddy.hashers/check attempt encrypted)))

(defn binding-map []
  {#'password-hasher/*password-hasher* (PasswordHasher.)})
