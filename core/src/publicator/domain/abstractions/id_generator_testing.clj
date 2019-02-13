(ns publicator.domain.abstractions.id-generator-testing
  (:require
   [publicator.domain.abstractions.id-generator :as sut]
   [clojure.test :as t]))

(defn- return-value []
  (t/is (pos-int? (sut/generate :test-space))))

(defn tests [wrapper]
  (doseq [test [#'return-value]
          :let [test-name (-> test symbol name)
                test      (wrapper test)]]
    (t/testing test-name
      (test))))
