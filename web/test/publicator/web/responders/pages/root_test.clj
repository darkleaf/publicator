(ns publicator.web.responders.pages.root-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.responders.pages.root :as sut]
   [publicator.web.responders.base :as base]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(t/deftest show
  (let [result [:pages/root]
        args   []
        resp   (base/->resp result args)]
    (t/is (http-predicates/ok? resp))))
