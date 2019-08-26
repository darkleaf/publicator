(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> user/blank
                (agg/with-msgs [[:user/state :add :root :active]
                                [:user/login :add :root "john"]
                                [:user/password :add :root "some password"]
                                [:user/password-digest :add :root "some digest"]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
