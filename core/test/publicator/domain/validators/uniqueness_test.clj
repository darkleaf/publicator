(ns publicator.domain.validators.uniqueness-test
  (:require
   [publicator.domain.validators.uniqueness :as uniqueness]
   [publicator.domain.abstractions.uniqueness :refer [*is-unique*]]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.domain.aggregate :as agg]
   [clojure.test :as t]))

(t/deftest validator
  (let [validator (uniqueness/validator #{:attr})
        spec      {:type         :test-agg
                   :id-generator (constantly 42)}
        agg       (-> (agg/build spec)
                      (agg/change [[:db/add :root :attr :val]]
                                  agg/allow-everething))]
    (t/testing "unique"
      (binding [*is-unique* (constantly true)]
        (let [errors (d.validation/validate agg validator)]
          (t/is (empty? errors)))))
    (t/testing "not unique"
      (binding [*is-unique* (constantly false)]
        (let [errors (d.validation/validate agg validator)]
          (t/is (not-empty errors)))))))
