(ns publicator.web.responders.user.log-out-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.responders.base :as responders.base]
   [publicator.use-cases.test.factories :as factories]
   [publicator.use-cases.interactors.user.log-out :as interactor]
   [publicator.web.responders.shared-testing :as shared-testing]
   [ring.util.http-predicates :as http-predicates]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(t/deftest all-implemented
  (shared-testing/all-responders-are-implemented `interactor/process))

(t/deftest processed
  (let [result (factories/gen ::interactor/processed)
        resp   (responders.base/result->resp result)]
    (t/is (http-predicates/redirection? resp))))
