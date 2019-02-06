(ns publicator-ext.domain.abstractions.id-generator-fake-test
  (:require
   [publicator-ext.domain.abstractions.id-generator-fake :as sut]
   [publicator-ext.domain.abstractions.id-generator-testing :as testing]
   [clojure.test :as t]))

(t/use-fixtures :each (fn [t]
                        (with-bindings (sut/binding-map)
                          (t))))

(t/deftest id-generator
  (testing/testing))
