(ns publicator.web.controllers.pages.root-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.routing :as routing]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(t/deftest process
  (let [req  (mock.request/request :get "/")
        resp (routing/handler req)]
    (t/is (http-predicates/ok? resp))))
