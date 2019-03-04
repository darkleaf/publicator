(ns publicator.use-cases.abstractions.session-fake-test
  (:require
   [publicator.use-cases.abstractions.session-fake :as sut]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest storage
  (u.t/run 'publicator.use-cases.abstractions.session-testing
    (fn [t]
      (with-bindings (sut/binding-map)
        (t)))))
