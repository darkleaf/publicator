(ns publicator.web.requests.pages.root-test
  (:require
   [clojure.test :as t]
   [publicator.utils.test.instrument :as instrument]
   [ring.util.http-predicates :as http-predicates]
   [ring.mock.request :as mock.request]
   [publicator.web.handler :as handler]))

(t/use-fixtures :once instrument/fixture)

(t/deftest root
  (let [handler (handler/build {:test? true})
        req     (mock.request/request :get "/")
        resp    (handler req)]
    (t/is (http-predicates/ok? resp))))
