(ns publicator.stubs.user-queries
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q]))

(defn get-by-login [user]
  (reify
    user-q/GetByLogin
    (-get-by-login [_ _login] user)))
