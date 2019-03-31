(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"
                  :user/state    :active}]
        user    (-> (agg/build user/spec)
                    (agg/change tx-data agg/allow-everething))
        errors  (agg/validate user)]
    (t/is (empty? errors))))
