(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> user/blank
                (agg/with-msgs [[:agg/add-attr :root :user/state :active]
                                [:agg/add-attr :root :user/login "john"]
                                [:agg/add-attr :root :user/password "some password"]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
