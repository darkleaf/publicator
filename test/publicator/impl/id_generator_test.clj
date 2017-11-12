(ns publicator.impl.id-generator-test
  (:require
   [clojure.test :as t]
   [publicator.impl.id-generator :as sut]
   [publicator.impl.test-data-source :refer [data-source]]
   [publicator.domain.abstractions.id-generator :as id-generator]))

(t/deftest generate
  (with-bindings (sut/binding-map data-source)
    (t/is (pos-int? (id-generator/generate)))))
