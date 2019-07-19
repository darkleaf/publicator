(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> user/blank
                (agg/with [{:db/ident             :root
                            :user/state           :active
                            :user/login           "john"}])
                (agg/apply-msg {:type   :add-attr
                                :entity :root
                                :attr   :user/password
                                :value  "some password"})
                (agg/validate))]
    (t/is (agg/has-no-errors? agg))))
