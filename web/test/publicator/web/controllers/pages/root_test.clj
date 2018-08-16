(ns publicator.web.controllers.pages.root-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.controllers.pages.root :as sut]
   [ring.mock.request :as mock.request]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]
   [sibiro.core]
   [sibiro.extras]))

(t/use-fixtures :once instrument/fixture)

(def handler
  (-> sut/routes
      sibiro.core/compile-routes
      sibiro.extras/make-handler))

(t/deftest process
  (let [req  (mock.request/request :get "/")
        resp (handler req)]
    (t/is (http-predicates/ok? resp))))
