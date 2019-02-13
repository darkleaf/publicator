(ns publicator.domain.abstractions.instant-testing
  (:require
   [publicator.domain.abstractions.instant :as sut]
   [clojure.test :as t]))

(defn return-value []
  (t/is (inst? (sut/now))))

(defn tests [wrapper]
  (doseq [test [#'return-value]
          :let [test-name (-> test symbol name)
                test      (wrapper test)]]
    (t/testing test-name
      (test))))
