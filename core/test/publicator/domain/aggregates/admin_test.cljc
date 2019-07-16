(ns publicator.domain.aggregates.admin-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.admin :as admin]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> admin/blank
                (agg/with [{:db/ident    :root
                            :admin/state :active}])
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
