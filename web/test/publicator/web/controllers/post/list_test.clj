(ns publicator.web.controllers.post.list-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.routing :as routing]
   [publicator.use-cases.interactors.post.list :as interactor]
   [publicator.use-cases.test.factories :as factories]
   [ring.mock.request :as mock.request]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]))

(t/use-fixtures :once instrument/fixture)

(t/deftest process
  (let [req             (-> (mock.request/request :get "/posts"))
        [action & args] (routing/handler req)
        args-spec       (-> `interactor/process s/get-spec :args)]
    (t/is (= interactor/process action))
    (t/is (nil? (s/explain-data args-spec args)))))
