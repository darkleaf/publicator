(ns publicator.domain.abstractions.id-generator)

(defprotocol IdGenerator
  (-generate [this]))

(declare ^:dynamic *id-generator*)

(defn generate []
  (-generate *id-generator*))
