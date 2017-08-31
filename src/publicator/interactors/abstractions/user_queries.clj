(ns publicator.interactors.abstractions.user-queries
  (:require
   [publicator.domain.user :as user]
   [clojure.spec.alpha :as s]))

(defprotocol GetByLogin
  (-get-by-login [this login]))

(s/def ::get-by-login #(satisfies? GetByLogin %))

(defn get-by-login [this login]
  (s/assert (s/nilable ::user/attrs)
            (-get-by-login this login)))
