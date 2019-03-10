(ns publicator.domain.abstractions.test-impl.id-generator-fake-test
  (:require
   [publicator.domain.abstractions.test-impl.id-generator-fake :as id-generator-fake]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest id-generator
  (u.t/run 'publicator.domain.abstractions.testing.id-generator
    (fn [t]
      (with-bindings (id-generator-fake/binding-map)
        (t)))))
