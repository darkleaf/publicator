(ns publicator.core.domain.aggregates.user-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (agg/allocate :agg/user)
                (d/db-with [{:db/ident             :root
                             :user/state           :active
                             :user/role            :regular
                             :user/login           "john"
                             :user/password        "some password"
                             :user/password-digest "some digest"}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
