(ns publicator.controllers.post.list-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.factories :as factories]
   [publicator.fixtures :as fixtures]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]))

(t/use-fixtures :each fixtures/fake-bindings)

(t/deftest handler
  (let [handler (handler/build)
        _       (dotimes [_ 2] (factories/create-post))
        req     (mock.request/request :get "/posts")
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))
