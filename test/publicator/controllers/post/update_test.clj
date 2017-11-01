(ns publicator.controllers.post.update-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.transit :as transit]
   [publicator.interactors.post.update :as interactor]
   [publicator.factories :as factories]
   [publicator.fixtures :as fixtures]
   [publicator.interactors.services.user-session :as user-session]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest form
  (let [handler (handler/build)
        user    (factories/create-user)
        _       (user-session/log-in! user)
        post    (factories/create-post :author-id (:id user))
        req     (mock.request/request :get (str "/posts/" (:id post) "/edit"))
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))

(t/deftest handler
  (let [handler (handler/build)
        user    (factories/create-user)
        _       (user-session/log-in! user)
        post    (factories/create-post :author-id (:id user))
        params  (sgen/generate (s/gen ::interactor/params))
        req     (-> (mock.request/request :patch (str "/posts/" (:id post)))
                    (mock.request/body (transit/write-str params))
                    (mock.request/content-type "application/transit+json"))
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))
    (t/is (some? (get-in resp [:headers "Location"])))))
