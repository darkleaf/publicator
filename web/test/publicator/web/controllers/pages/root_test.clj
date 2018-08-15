(ns publicator.web.controllers.post.show-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.controllers.pages.root :as sut]
   [ring.mock.request :as mock.request]
   [clojure.test :as t]
   [sibiro.core]
   [sibiro.extras]))

(t/use-fixtures :once instrument/fixture)

(def handler
  (-> sut/routes
      sibiro.core/compile-routes
      sibiro.extras/make-handler))

(t/deftest process
  (let [req             (-> (mock.request/request :get "/"))
        [action & args] (handler req)]
    (t/is (fn? action))
    (t/is (empty? args))))
