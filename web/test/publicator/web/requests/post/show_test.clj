(ns publicator.web.requests.post.show-test
  (:require
   [publicator.utils.test.instrument]
   [clojure.test :as t]
   [ring.util.http-predicates :as http-predicates]
   [ring.mock.request :as mock.request]
   [publicator.web.handler :as handler]
   [publicator.use-cases.interactors.post.show :as interactor]
   [publicator.use-cases.test.factories :as factories]))

(t/deftest handler
  (let [handler (handler/build)
        req     (mock.request/request :get "/posts/1")
        called? (atom false)
        process (fn [id]
                  (reset! called? true)
                  (t/is (= 1 id))
                  (factories/gen ::interactor/processed))
        resp    (binding [interactor/*process* process]
                  (handler req))]
    (t/is @called?)
    (t/is (http-predicates/ok? resp))))

(t/deftest handler-not-found
  (let [handler (handler/build)
        req     (mock.request/request :get "/posts/1")
        called? (atom false)
        process (fn [id]
                  (reset! called? true)
                  (factories/gen ::interactor/not-found))
        resp    (binding [interactor/*process* process]
                  (handler req))]
    (t/is @called?)
    (t/is (http-predicates/not-found? resp))))
