(ns publicator.persistence.id-generator-test
  (:require
   [clojure.test :as t]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.utils.test.instrument :as instrument]
   [publicator.persistence.test.db :as db]
   [publicator.persistence.id-generator :as sut]))

(defn- setup [t]
  (with-bindings (sut/binding-map db/*data-source*)
    (t)))

(t/use-fixtures :once
  instrument/fixture
  db/once-fixture)

(t/use-fixtures :each
  db/each-fixture
  setup)

(t/deftest generate
  (t/is (pos-int? (id-generator/generate))))
