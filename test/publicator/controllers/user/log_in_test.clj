(ns publicator.controllers.user.log-in-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.transit :as transit]
   [publicator.interactors.user.log-in :as interactor]
   [publicator.factories :as factories]
   [publicator.fixtures :as fixtures]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

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
        req      (-> (mock.request/request :post "/log-in")
                     (mock.request/body (transit/write-str params))
                     (mock.request/content-type "application/transit+json"))
        resp     (handler req)]
    (t/is (util.http-predicates/ok? resp))
    (t/is (some? (get-in resp [:headers "Location"])))))
