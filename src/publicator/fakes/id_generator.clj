(ns publicator.fakes.id-generator
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]))

(deftype IdGenerator [counter]
  id-generator/IdGenerator

  (-generate [_]
    (swap! counter inc)))

(defn build []
  (IdGenerator. (atom 0)))

(defn binging-map []
  {#'id-generator/*id-generator* (build)})
