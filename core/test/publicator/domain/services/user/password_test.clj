(ns publicator.domain.services.user.password-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.services.user.password :as user.password]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [clojure.test :as t]
   [datascript.core :as d]))

(t/use-fixtures :each scaffolding/setup)

(t/deftest test
  (t/testing "ok"
    (let [tx-data [{:db/ident      :root
                    :user/login    "john"
                    :user/state    :active
                    :user/password "password"}]
          user    (-> (agg/build user/spec)
                      (agg/change tx-data agg/allow-everething)
                      (user.password/process))
          errors  (agg/validate user user.password/validator)]
      (t/is (empty? errors))))
  (t/testing "with digest"
    (let [tx-data [{:db/ident             :root
                    :user/login           "john"
                    :user/state           :active
                    :user/password-digest "some-digest"}]
          user    (-> (agg/build user/spec)
                      (agg/change tx-data agg/allow-everething)
                      (user.password/process))
          errors  (agg/validate user user.password/validator)]
      (t/is (empty? errors))))
  (t/testing "required password"
    (let [tx-data [{:db/ident   :root
                    :user/login "john"
                    :user/state :active}]
          user    (-> (agg/build user/spec)
                      (agg/change tx-data agg/allow-everething)
                      (user.password/process))
          errors  (agg/validate user user.password/validator)]
      (t/is (not-empty errors))))
  (t/testing "wrong password"
    (let [tx-data [{:db/ident      :root
                    :user/login    "john"
                    :user/state    :active
                    :user/password ""}]
          user    (-> (agg/build user/spec)
                      (agg/change tx-data agg/allow-everething)
                      (user.password/process))
          errors  (agg/validate user user.password/validator)]
      (t/is (not-empty errors)))))
