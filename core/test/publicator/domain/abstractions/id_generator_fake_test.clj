(ns publicator.domain.abstractions.id-generator-fake-test
  (:require
   [publicator.domain.abstractions.id-generator-fake :as sut]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest id-generator
  (u.t/run 'publicator.domain.abstractions.id-generator-testing
    (fn [t]
      (with-bindings (sut/binding-map)
        (t)))))
