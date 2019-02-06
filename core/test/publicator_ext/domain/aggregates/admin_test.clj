(ns publicator-ext.domain.aggregates.admin-test
  (:require
   [publicator-ext.domain.aggregates.admin :as sut]
   [publicator-ext.domain.abstractions.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest build
  (let [user-id 1
        tx-data [{:db/ident    :root
                  :admin/state :active}]
        admin   (sut/build user-id tx-data)]
    (t/is (some? admin))))
