(ns publicator.use-cases.abstractions.session-testing
  (:require
   [publicator.use-cases.abstractions.session :as sut]
   [clojure.test :as t]))

(defn test-get-&-set []
  (sut/*set* :key :value)
  (t/is (= :value (sut/*get* :key))))
