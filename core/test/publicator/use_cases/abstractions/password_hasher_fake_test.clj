(ns publicator.use-cases.abstractions.password-hasher-fake-test
  (:require
   [publicator.use-cases.abstractions.password-hasher-fake :as sut]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest storage
  (u.t/run 'publicator.use-cases.abstractions.password-hasher-testing
    (fn [t]
      (with-bindings (sut/binding-map)
        (t)))))
