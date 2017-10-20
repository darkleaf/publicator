(ns publicator.interactors.abstractions.user-queries)

(defprotocol GetByLogin
  (-get-by-login [this login]))

(declare ^:dynamic *get-by-login*)

(defn get-by-login [login]
  (-get-by-login *get-by-login* login))
