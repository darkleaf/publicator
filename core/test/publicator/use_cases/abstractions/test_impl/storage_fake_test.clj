(ns publicator.use-cases.abstractions.test-impl.storage-fake-test
  (:require
   [publicator.use-cases.abstractions.test-impl.storage-fake :as storage-fake]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(t/deftest storage
  (u.t/run 'publicator.use-cases.abstractions.testing.storage
    (t/join-fixtures [scaffolding/setup
                      (fn [t]
                        (let [db (storage-fake/build-db)]
                          (with-bindings (storage-fake/binding-map db)
                            (t))))])))
