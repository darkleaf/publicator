(ns publicator.interactors.abstractions.session
  (:refer-clojure :exclude [read]))

(defprotocol PSession
  (read [this k])
  (write! [this k v]))

#_(defn update! [this k updater])
