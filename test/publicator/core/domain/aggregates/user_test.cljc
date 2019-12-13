(ns publicator.core.domain.aggregates.user-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/allocate :agg/new-user)
                (agg/apply-tx [{:db/ident             :root
                                :user/state           :active
                                :user/role            :regular
                                :user/login           "john"
                                :user/password        "some password"
                                :user/password-digest "some digest"}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
