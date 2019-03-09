(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "12345678"}]
        user    (agg/build user/spec tx-data)]
    (t/is (some? user))))
