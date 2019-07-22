(ns publicator.domain.aggregates.admin-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.admin :as admin]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> admin/blank
                (agg/with-msgs [[:admin/state :root :active]])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
