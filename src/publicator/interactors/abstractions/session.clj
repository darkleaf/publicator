(ns publicator.interactors.abstractions.session)

(defprotocol PSession
  (read [this k])
  (write! [this k v]))

#_(defn update! [this k updater])
