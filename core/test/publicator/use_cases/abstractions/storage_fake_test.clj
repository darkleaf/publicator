(ns publicator.use-cases.abstractions.storage-fake-test
  (:require
   [publicator.use-cases.abstractions.storage-fake :as sut]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

;; (t/deftest storage
;;   (u.t/run 'publicator.use-cases.abstractions.storage-testing
;;     (t/join-fixtures [scaffolding/setup
;;                       (fn [t]
;;                         (let [db (sut/build-db)]
;;                           (with-bindings (sut/binding-map db)
;;                             (t))))])))
