(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> user/blank
                (agg/apply-msgs [[:add-attr :root :user/state :active]
                                 [:add-attr :root :user/login "john"]
                                 [:add-attr :root :user/password "some password"]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
