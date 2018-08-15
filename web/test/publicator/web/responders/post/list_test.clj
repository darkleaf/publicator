(ns publicator.web.responders.post.list-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.responders.post.list :as sut]
   [publicator.web.responders.base :as base]
   [publicator.use-cases.test.factories :as factories]
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.use-cases.test.fakes :as fakes]
   [publicator.web.responders.shared-testing :as shared-testing]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)
(t/use-fixtures :each fakes/fixture)

(t/deftest all-implemented
  (shared-testing/all-responders-are-implemented `interactor/process))

(t/deftest processed
  (let [result (factories/gen ::interactor/processed)
        args   []
        resp   (base/->resp result args)]
    (t/is (http-predicates/ok? resp))))
