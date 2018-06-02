(ns publicator.web.requests.post.list-test
  (:require
   [publicator.utils.test.instrument]
   [clojure.test :as t]
   [ring.util.http-predicates :as http-predicates]
   [ring.mock.request :as mock.request]
   [publicator.web.handler :as handler]
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.use-cases.test.factories :as factories]))

(t/deftest handler
  (let [handler (handler/build)
        req     (mock.request/request :get "/posts")
        called? (atom false)
        process (fn []
                  (reset! called? true)
                  (factories/gen ::interactor/processed))
        resp    (binding [interactor/*process* process]
                  (handler req))]
    (t/is @called?)
    (t/is (http-predicates/ok? resp))))
