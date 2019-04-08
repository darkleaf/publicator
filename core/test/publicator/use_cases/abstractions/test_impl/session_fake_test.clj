(ns publicator.use-cases.abstractions.test-impl.session-fake-test
  (:require
   [publicator.use-cases.abstractions.test-impl.session-fake :as session-fake]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest storage
  (u.t/run 'publicator.use-cases.abstractions.testing.session
    (fn [t]
      (with-bindings (session-fake/binding-map)
        (t)))))
