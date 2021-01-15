(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest remove-errors
  (let [agg   (-> agg/proto-agg
                  (d/db-with [{:error/entity :root}]))]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))

(t/deftest predicate-validator
  (let [validators (-> agg/proto-validators
                       (d/db-with [[:predicate/upsert :test/attr #{:ok}]]))
        agg        (d/db-with agg/proto-agg [[:db/add 2 :test/attr :wrong]
                                             [:db/add 3 :test/attr :ok]])]
    (t/testing "main"
      (let [agg (agg/validate agg validators)]
        (t/is (= [(d/datom 4 :error/attribute :test/attr)
                  (d/datom 4 :error/entity 2)
                  (d/datom 4 :error/type :predicate)
                  (d/datom 4 :error/value :wrong)]
                 (d/seek-datoms agg :eavt 4)))))
    (t/testing "upsert"
      (let [validators (d/db-with validators
                                  [[:predicate/upsert :test/attr #{:wrong}]])
            agg (agg/validate agg validators)]
        (t/is (= [(d/datom 4 :error/attribute :test/attr)
                  (d/datom 4 :error/entity 3)
                  (d/datom 4 :error/type :predicate)
                  (d/datom 4 :error/value :ok)]
                 (d/seek-datoms agg :eavt 4)))))))

(t/deftest required-validator
  (let [validators (-> agg/proto-validators
                       (d/db-with [[:required/upsert :test/a agg/root-entity]]))]
    (t/testing "main"
      (let [agg (-> agg/proto-agg
                    (d/db-with [{:db/id  2
                                 :test/a :value}])
                    (agg/validate validators))]
        (t/is (= [(d/datom 3 :error/attribute :test/a)
                  (d/datom 3 :error/entity 1) ;; root entity
                  (d/datom 3 :error/type :required)]
                 (d/seek-datoms agg :eavt 3)))))
    (t/testing "upsert"
      (let [not-root-entities '[[(entity ?e)
                                 [?e _ _]
                                 (not [?e :db/ident :root])]]
            validators        (d/db-with validators
                                         [[:required/upsert :test/a not-root-entities]])
            agg               (-> agg/proto-agg
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
  (t/is (= agg/proto-validators
           (-> agg/proto-validators
               (d/db-with [[:predicate/upsert :test/a #{:ok}]
                           [:required/upsert  :test/a agg/root-entity]
                           [:retract/by-attribute :test/a]])))))
