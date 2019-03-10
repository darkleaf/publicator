(ns publicator.domain.abstractions.test-impl.password-hasher-fake-test
  (:require
   [publicator.domain.abstractions.test-impl.password-hasher-fake :as password-hasher-fake]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest password-hasher
  (u.t/run 'publicator.domain.abstractions.testing.password-hasher
    (fn [t]
      (with-bindings (password-hasher-fake/binding-map)
        (t)))))
