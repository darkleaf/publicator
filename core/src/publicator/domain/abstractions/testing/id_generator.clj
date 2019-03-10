(ns publicator.domain.abstractions.testing.id-generator
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [clojure.test :as t]))

(defn test-return-value []
  (t/is (pos-int? (id-generator/*generate* :test-space))))
