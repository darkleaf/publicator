(ns publicator-ext.domain.abstractions.id-generator
  (:require
   [clojure.spec.alpha :as s]))

(def +spaces+ #{:user :stream :publication})

(defprotocol IdGenerator
  (-generate [this space]))

(declare ^:dynamic *id-generator*)

(s/def ::id pos-int?)
(s/def ::space +spaces+)

(s/fdef generate
  :args (s/cat :space ::space)
  :ret ::id)

(defn generate [space]
  (-generate *id-generator* space))
