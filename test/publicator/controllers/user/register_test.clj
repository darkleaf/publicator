(ns publicator.controllers.user.register-test
  (:require
   [publicator.controllers.test-ring :refer [build-handler]]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]))

(t/deftest form
  (let [handler (build-handler)
        req     (mock.request/request :get "/registration")
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))

(t/deftest handler
  (let [handler (build-handler)
        req     (mock.request/request :post "/registration")
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))
    (t/is (some? (get-in resp [:headers "Location"])))))
