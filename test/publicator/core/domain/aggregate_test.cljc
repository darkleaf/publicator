(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest remove-errors
  (let [agg   (-> (agg/new-aggregate)
                  (d/db-with [{:error/entity :root}]))]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))

(t/deftest predicate-validator
  (let [validators (-> (agg/new-validators)
                       (agg/upsert-predicate-validator :test/attr #{:ok}))
        agg        (-> (agg/new-aggregate)
                       (d/db-with [[:db/add 2 :test/attr :wrong]
                                   [:db/add 3 :test/attr :ok]]))]
    (t/testing "main"
      (let [agg (agg/validate agg validators)]
        (t/is (= [(d/datom 4 :error/attribute :test/attr)
                  (d/datom 4 :error/entity 2)
                  (d/datom 4 :error/type :predicate)
                  (d/datom 4 :error/value :wrong)]
                 (d/seek-datoms agg :eavt 4)))))
    (t/testing "upsert"
      (let [validators (-> validators
                           (agg/upsert-predicate-validator :test/attr #{:wrong}))
            agg (agg/validate agg validators)]
        (t/is (= [(d/datom 4 :error/attribute :test/attr)
                  (d/datom 4 :error/entity 3)
                  (d/datom 4 :error/type :predicate)
                  (d/datom 4 :error/value :ok)]
                 (d/seek-datoms agg :eavt 4)))))))

(t/deftest required-validator
  (let [validators (-> (agg/new-validators)
                       (agg/upsert-required-validator :test/a agg/root-entity-rule))]
    (t/testing "main"
      (let [agg (-> (agg/new-aggregate)
                    (d/db-with [{:db/id  2
                                 :test/a :value}])
                    (agg/validate validators))]
        (t/is (= [(d/datom 3 :error/attribute :test/a)
                  (d/datom 3 :error/entity 1) ;; root entity
                  (d/datom 3 :error/type :required)]
                 (d/seek-datoms agg :eavt 3)))))
    (t/testing "upsert"
      (let [not-root-entity-rule '[[(entity ?e)
                                    [?e _ _]
                                    (not [?e :db/ident :root])]]

            validators (-> validators
                           (agg/upsert-required-validator :test/a not-root-entity-rule))
            agg        (-> (agg/new-aggregate)
                           (d/db-with [{:db/ident :root
                                        :test/a   :value}
                                       {:db/id      2
                                        :test/not-a 42}])
                           (agg/validate validators))]
        (t/is (= [(d/datom 3 :error/attribute :test/a)
                  (d/datom 3 :error/entity 2)
                  (d/datom 3 :error/type :required)]
                 (d/seek-datoms agg :eavt 3)))))))

(t/deftest retract-by-attribute
  (t/is (= (agg/new-validators)
           (-> (agg/new-validators)
               (agg/upsert-predicate-validator :test/a #{:ok})
               (agg/upsert-required-validator  :test/a agg/root-entity-rule)
               (agg/retract-validators-by-attribute :test/a)))))
