(ns publicator.controllers.post.show-test
  (:require
   [publicator.ring.handler :as handler]
   [publicator.factories :as factories]
   [publicator.fixtures :as fixtures]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as util.http-predicates]
   [clojure.test :as t]))

(t/use-fixtures :each fixtures/all)

(t/deftest handler
  (let [handler (handler/build)
        post    (factories/create-post)
        req     (mock.request/request :get (str "/posts/" (:id post)))
        resp    (handler req)]
    (t/is (util.http-predicates/ok? resp))))
