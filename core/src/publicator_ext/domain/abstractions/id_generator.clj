(ns publicator-ext.domain.abstractions.id-generator)

(defprotocol IdGenerator
  (-generate [this space]))

(declare ^:dynamic *id-generator*)

(defn generate [space]
  (-generate *id-generator* space))
