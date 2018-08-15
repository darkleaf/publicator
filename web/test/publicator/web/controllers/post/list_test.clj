(ns publicator.web.controllers.post.list-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.controllers.post.list :as sut]
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.use-cases.test.factories :as factories]
   [ring.mock.request :as mock.request]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [sibiro.core]
   [sibiro.extras]))

(t/use-fixtures :once instrument/fixture)

(def handler
  (-> sut/routes
      sibiro.core/compile-routes
      sibiro.extras/make-handler))

(t/deftest process
  (let [req             (-> (mock.request/request :get "/posts"))
        [action & args] (handler req)
        args-spec       (-> `interactor/process s/get-spec :args)]
    (t/is (= interactor/process action))
    (t/is (nil? (s/explain-data args-spec args)))))
