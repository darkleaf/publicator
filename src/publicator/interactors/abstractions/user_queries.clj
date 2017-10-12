(ns publicator.interactors.abstractions.user-queries)

(defprotocol GetByLogin
  (get-by-login [this login]))
