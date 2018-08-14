(ns publicator.web.responders.post.update-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.responders.post.update :as sut]
   [publicator.web.responders.base :as base]
   [publicator.use-cases.test.factories :as factories]
   [publicator.use-cases.interactors.post.update :as interactor]
   [publicator.web.responders.shared-testing :as shared-testing]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]))

(t/use-fixtures :once instrument/fixture)

(t/deftest all-implemented
  (shared-testing/all-responders-are-implemented `interactor/initial-params)
  (shared-testing/all-responders-are-implemented `interactor/process))

(t/deftest initial-params
  (let [result (factories/gen ::interactor/initial-params)
        args   [1]
        resp   (base/->resp result args)]
    (t/is (http-predicates/ok? resp))))

(t/deftest invalid-params
  (let [result [::interactor/invalid-params (s/explain-data ::interactor/params {})]
        args   [1]
        resp   (base/->resp result args)]
    (t/is (http-predicates/unprocessable-entity? resp))))

(t/deftest processed
  (let [result (factories/gen ::interactor/processed)
        args   [1]
        resp   (base/->resp result args)]
    (t/is (http-predicates/created? resp))))
