(ns publicator.domain.aggregates.admin-test
  (:require
   [publicator.domain.aggregates.admin :as admin]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [user-id 1
        tx-data [{:db/ident :root
                  :root/id  user-id}]
        admin   (agg/build! admin/spec tx-data)]
    (t/is (some? admin))))
