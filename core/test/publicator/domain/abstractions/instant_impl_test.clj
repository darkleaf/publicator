(ns publicator.domain.abstractions.instant-impl-test
  (:require
   [publicator.domain.abstractions.instant-impl :as sut]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest instant
  (u.t/run 'publicator.domain.abstractions.instant-testing
    (fn [t]
      (with-bindings (sut/binding-map)
        (t)))))
