(ns publicator.web.controllers.user.log-out-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.controllers.user.log-out :as sut]
   [publicator.use-cases.interactors.user.log-out :as interactor]
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
  (let [req             (-> (mock.request/request :post "/log-out"))
        [action & args] (handler req)
        args-spec       (-> `interactor/process s/get-spec :args)]
    (t/is (= interactor/process action))
    (t/is (nil? (s/explain-data args-spec args)))))
