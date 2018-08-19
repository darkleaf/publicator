(ns publicator.web.controllers.user.register-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.routing :as routing]
   [publicator.use-cases.interactors.user.register :as interactor]
   [publicator.use-cases.test.factories :as factories]
   [ring.mock.request :as mock.request]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [sibiro.core]
   [sibiro.extras]))

(t/use-fixtures :once instrument/fixture)

(t/deftest initial-params
  (let [req             (mock.request/request :get "/register")
        [action & args] (routing/handler req)
        args-spec       (-> `interactor/initial-params s/get-spec :args)]
    (t/is (= interactor/initial-params action))
    (t/is (nil? (s/explain-data args-spec args)))))

(t/deftest process
  (let [params          (factories/gen ::interactor/params)
        req             (-> (mock.request/request :post "/register")
                            (assoc :transit-params params))
        [action & args] (routing/handler req)
        args-spec       (-> `interactor/process s/get-spec :args)]
    (t/is (= interactor/process action))
    (t/is (nil? (s/explain-data args-spec args)))))
