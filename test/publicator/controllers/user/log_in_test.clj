(ns publicator.controllers.user.log-in-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.interactors.user.log-in :as interactor]
   [publicator.factories :as factories]
   [publicator.fixtures :as fixtures]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [form-ujs.ring]
   [clojure.test :as t]))

(t/use-fixtures :each fixtures/all)

(t/deftest form
  (let [handler (handler/build)
        req     (mock.request/request :get "/log-in")
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))

(t/deftest handler
  (let [handler  (handler/build)
        password "cool-password"
        user     (factories/create-user :password password)
        params   {:login    (:login user)
                  :password password}
        req      (form-ujs.ring/data->request :post "/log-in" params)
        resp     (handler req)]
    (t/is (form-ujs.ring/successful-response? resp))))
