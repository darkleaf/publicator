(ns publicator.controllers.user.log-out-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.transit :as transit]
   [publicator.factories :as factories]
   [publicator.fixtures :as fixtures]
   [publicator.interactors.services.user-session :as user-session]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest handler
  (let [handler (handler/build)
        user    (factories/create-user)
        _       (user-session/log-in! user)
        req     (-> (mock.request/request :post "/log-out"))
        resp    (handler req)]
    (t/is (util.http-predicates/redirection? resp))
    (t/is (some? (get-in resp [:headers "Location"])))))
