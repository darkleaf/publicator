(ns publicator.domain.abstractions.password-hasher-fake-test
  (:require
   [publicator.domain.abstractions.password-hasher-fake :as password-hasher-fake]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest storage
  (u.t/run 'publicator.domain.abstractions.password-hasher-testing
    (fn [t]
      (with-bindings (password-hasher-fake/binding-map)
        (t)))))
