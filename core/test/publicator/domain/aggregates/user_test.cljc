(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> user/new-blank
                (agg/with [{:db/ident             :root
                            :user/state           :active
                            :user/login           "john"
                            :user/password        "some password"
                            :user/password-digest "some digest"}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
