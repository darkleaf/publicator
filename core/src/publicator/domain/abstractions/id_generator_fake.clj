(ns publicator.domain.abstractions.id-generator-fake
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]))

(deftype IdGenerator [ids]
  id-generator/IdGenerator
  (-generate [_ space]
    (-> ids
        (swap! update space (fnil inc 0))
        (get space))))

(defn build []
  (IdGenerator. (atom {})))

(defn binding-map []
  {#'id-generator/*id-generator* (build)})
