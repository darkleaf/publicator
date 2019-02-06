(ns publicator-ext.domain.aggregates.user-test
  (:require
   [publicator-ext.domain.aggregates.user :as sut]
   [publicator-ext.domain.abstractions.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident             :root
                  :user/login           "john"
                  :user/password-digest "12345678"
                  :user/state           :active}]
        user    (sut/build tx-data)]
    (t/is (some? user))))
