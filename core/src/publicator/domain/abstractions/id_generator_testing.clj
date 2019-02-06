(ns publicator.domain.abstractions.id-generator-testing
  (:require
   [publicator.domain.abstractions.id-generator :as sut]
   [clojure.test :as t]))

(defn testing []
  (doseq [space #{:user :stream :publication}]
    (t/testing space
      (t/is (pos-int? (sut/generate space))))))
