(ns publicator.web.middlewares.transit-params-test
  (:require
   [publicator.web.middlewares.transit-params :as sut]
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.transit :as transit]
   [ring.mock.request :as mock.request]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(t/deftest transit
  (let [data    {:foo :bar}
        req     (-> (mock.request/request :post "/foo/bar")
                    (mock.request/content-type "application/transit+json")
                    (mock.request/body (transit/write data)))
        handler (sut/wrap-transit-params identity)
        resp    (handler req)]
    (t/is (= data (:transit-params resp)))))

(t/deftest other
  (let [req     (mock.request/request :post "/foo/bar")
        handler (sut/wrap-transit-params identity)
        resp    (handler req)]
    (t/is (not (contains? resp :transit-params)))))
