(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> user/blank
                (agg/with-msgs [[:user/state :root :active]
                                [:user/login :root "john"]
                                [:user/password :root "some password"]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
