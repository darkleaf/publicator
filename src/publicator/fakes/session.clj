(ns publicator.fakes.session
  (:require
   [publicator.interactors.abstractions.session :as session]))

(deftype FakeSession [storage]
  session/PSession
  (read [_ k] (get @storage k))
  (write! [_ k v] (swap! storage assoc k v)))

(defn build [] (->FakeSession (atom {})))
