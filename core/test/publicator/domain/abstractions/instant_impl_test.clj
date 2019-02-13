(ns publicator.domain.abstractions.instant-impl-test
  (:require
   [publicator.domain.abstractions.instant-impl :as sut]
   [publicator.domain.abstractions.instant-testing :as testing]
   [clojure.test :as t]))

(t/deftest instant
  (testing/tests (fn [t]
                   (fn []
                     (with-bindings (sut/binding-map)
                       (t))))))
