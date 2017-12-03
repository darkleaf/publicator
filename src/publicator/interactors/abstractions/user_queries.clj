(ns publicator.interactors.abstractions.user-queries
  (:require
   [publicator.domain.user])
  (:import
   [publicator.domain.user User]))

(defprotocol GetByLogin
  (-get-by-login [this login]))

(declare ^:dynamic *get-by-login*)

(defn get-by-login [login]
  {:post [(or (nil? %)
              (instance? User %))]}
  (-get-by-login *get-by-login* login))
