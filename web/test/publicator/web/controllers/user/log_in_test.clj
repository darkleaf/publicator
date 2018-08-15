(ns publicator.web.controllers.user.log-in-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.controllers.user.log-in :as sut]
   [publicator.use-cases.interactors.user.log-in :as interactor]
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

(t/deftest initial-params
  (let [req             (mock.request/request :get "/log-in")
        [action & args] (handler req)
        args-spec       (-> `interactor/initial-params s/get-spec :args)]
    (t/is (= interactor/initial-params action))
    (t/is (nil? (s/explain-data args-spec args)))))

(t/deftest process
  (let [params          (factories/gen ::interactor/params)
        req             (-> (mock.request/request :post "/log-in")
                            (assoc :transit-params params))
        [action & args] (handler req)
        args-spec       (-> `interactor/process s/get-spec :args)]
    (t/is (= interactor/process action))
    (t/is (nil? (s/explain-data args-spec args)))))
