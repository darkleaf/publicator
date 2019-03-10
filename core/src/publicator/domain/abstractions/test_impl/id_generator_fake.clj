(ns publicator.domain.abstractions.test-impl.id-generator-fake
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]))

(defn- build []
  (let [counters (atom {})]
    (fn [space]
      (-> counters
          (swap! update space (fnil inc 0))
          (get space)))))

(defn binding-map []
  {#'id-generator/*generate* (build)})
