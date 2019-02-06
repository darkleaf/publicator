(ns publicator.domain.abstractions.instant-impl-test
  (:require
   [publicator.domain.abstractions.instant-impl :as sut]
   [publicator.domain.abstractions.instant-testing :as testing]
   [clojure.test :as t]))

(t/use-fixtures :each (fn [t]
                        (with-bindings (sut/binding-map)
                          (t))))

(t/deftest instant
  (testing/testing))
