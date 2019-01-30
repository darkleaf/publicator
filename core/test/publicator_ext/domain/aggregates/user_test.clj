(ns publicator-ext.domain.aggregates.user-test
  (:require
   [publicator-ext.domain.aggregates.user :as sut]
   [publicator-ext.domain.test.fakes :as fakes]
   [publicator-ext.utils.test.instrument :as instrument]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest build
  (let [tx-data [{:db/ident             :root
                  :user/login           "john"
                  :user/password-digest "12345678"
                  :user/state           :active}]
        user    (sut/build tx-data)]
    (t/is (some? user))))
