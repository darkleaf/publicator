(ns publicator.web.responders.post.show-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.responders.post.show :as sut]
   [publicator.web.responders.base :as base]
   [publicator.use-cases.test.factories :as factories]
   [publicator.use-cases.interactors.post.show :as interactor]
   [publicator.web.responders.shared-testing :as shared-testing]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(t/deftest all-implemented
  (shared-testing/all-responders-are-implemented `interactor/process))

(t/deftest processed
  (let [result (factories/gen ::interactor/processed)
        args   []
        resp   (base/->resp result args)]
    (t/is (http-predicates/ok? resp))))
