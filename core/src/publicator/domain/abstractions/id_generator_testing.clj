(ns publicator.domain.abstractions.id-generator-testing
  (:require
   [publicator.domain.abstractions.id-generator :as sut]
   [clojure.test :as t]))

(defn test-return-value []
  (t/is (pos-int? (sut/*generate* :test-space))))
