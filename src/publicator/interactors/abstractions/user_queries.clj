(ns publicator.interactors.abstractions.user-queries
  (:require
   [publicator.domain.user :as user]
   [clojure.spec.alpha :as s]))

(defprotocol PGetByLogin
  (-get-by-login [this login]))

(defn get-by-login [this login]
  (s/assert ::user/attrs
            (-get-by-login this login)))
