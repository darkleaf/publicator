(ns publicator.controllers.user.register-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.transit :as transit]
   [publicator.interactors.user.register :as interactor]
   [publicator.fixtures :as fixtures]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest form
  (let [handler (handler/build)
        req     (mock.request/request :get "/registration")
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))

(t/deftest handler
  (let [handler (handler/build)
        params  (sgen/generate (s/gen ::interactor/params))
        req     (-> (mock.request/request :post "/registration")
                    (mock.request/body (transit/write-str params))
                    (mock.request/content-type "application/transit+json"))
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))
    (t/is (some? (get-in resp [:headers "Location"])))))
