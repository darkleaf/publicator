(ns publicator.controllers.user.register-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.interactors.user.register :as interactor]
   [publicator.fixtures :as fixtures]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [form-ujs.ring]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/fake-bindings)

(t/deftest form
  (let [handler (handler/build)
        req     (mock.request/request :get "/registration")
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))

(t/deftest handler
  (let [handler (handler/build)
        params  (sgen/generate (s/gen ::interactor/params))
        req     (form-ujs.ring/data->request :post "/registration" params)
        resp    (handler req)]
    (t/is (form-ujs.ring/successful-response? resp))))
