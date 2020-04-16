(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(swap! agg/schema merge
       {:allocate-test/many {:db/cardinality :db.cardinality/many}})

(t/deftest allocate
  (let [agg (agg/allocate)]
    (t/is (some? agg))
    (t/is (-> agg :schema (contains? :allocate-test/many)))
    (t/is (= 1 (d/q '[:find ?e . :where [?e :db/ident :root]]
                    agg)))))


(t/deftest remove-errors
  (let [agg (-> (agg/allocate)
                (d/db-with [{:error/entity :root}]))]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))


(swap! agg/schema merge
       {:predicate-validator-test/attr {:agg/predicate #{:ok}}})

(t/deftest predicate-validator
  (let [agg (-> (agg/allocate)
                (d/db-with [[:db/add 2 :predicate-validator-test/attr :wrong]
                            [:db/add 3 :predicate-validator-test/attr :ok]])
                (agg/predicate-validator))]
    (t/is (= [[4 :error/attr :predicate-validator-test/attr]
              [4 :error/entity 2]
              [4 :error/type :predicate]
              [4 :error/value :wrong]]
             (->> (d/seek-datoms agg :eavt 4)
                  (map (juxt :e :a :v)))))))


(swap! agg/schema merge
       {:uniq-validator-test/attr {:db/cardinality :db.cardinality/many
                                   :agg/uniq       true}})

(t/deftest uniq-validator
  (let [agg (-> (agg/allocate)
                (d/db-with [{:uniq-validator-test/attr  1
                             :uniq-validator-test/other 1}
                            {:uniq-validator-test/attr  [2 3]
                             :uniq-validator-test/other 1}
                            {:uniq-validator-test/attr  [1 3]
                             :uniq-validator-test/other 1}])
                (agg/uniq-validator))]
    (t/is (= [[5 :error/attr :uniq-validator-test/attr]
              [5 :error/entity 4]
              [5 :error/type :uniq]
              [5 :error/value 1]

              [6 :error/attr :uniq-validator-test/attr]
              [6 :error/entity 4]
              [6 :error/type :uniq]
              [6 :error/value 3]]
             (->> (d/seek-datoms agg :eavt 5)
                  (map (juxt :e :a :v)))))))


(swap! agg/schema merge
       {:required-validator-test.nested/root   {:db/valueType :db.type/ref}
        :required-validator-test.nested/status {:db/index true}})

(t/deftest required-validator
  (let [agg (-> (agg/allocate)
                (d/db-with [{:db/ident                  :root
                             :required-validator-test/a :ok}
                            {:db/id                               2
                             :required-validator-test/c           :ok
                             :required-validator-test.nested/root :root}
                            {:db/id                               3
                             :required-validator-test/d           :ok
                             :required-validator-test.nested/root :root}
                            {:db/id                                 4
                             :required-validator-test/c             :ok
                             :required-validator-test/d             :ok
                             :required-validator-test.nested/root   :root
                             :required-validator-test.nested/status :ready}])
                (agg/required-validator {:root
                                         [:required-validator-test/a :required-validator-test/b]

                                         :required-validator-test.nested/_root
                                         [:required-validator-test/c :required-validator-test/d]

                                         [:required-validator-test.nested/status :ready]
                                         [:required-validator-test/e]}))]
    (t/is (= [[5 :error/attr :required-validator-test/b]
              [5 :error/entity 1]
              [5 :error/type :required]

              [6 :error/attr :required-validator-test/d]
              [6 :error/entity 2]
              [6 :error/type :required]

              [7 :error/attr :required-validator-test/c]
              [7 :error/entity 3]
              [7 :error/type :required]

              [8 :error/attr :required-validator-test/e]
              [8 :error/entity 4]
              [8 :error/type :required]]
             (->> (d/seek-datoms agg :eavt 5)
                  (map (juxt :e :a :v)))))))


(swap! agg/schema merge
       {:count-validator-test.nested/root {:db/valueType :db.type/ref}})

(t/deftest count-validator
  (let [agg (-> (agg/allocate)
                (d/db-with [{:count-validator-test.nested/root :root
                             :count-validator-test.nested/attr 1}
                            {:count-validator-test.nested/root :root
                             :count-validator-test.nested/attr 1}])
                (agg/count-validator :count-validator-test.nested/attr 3))]
    (t/is (= [[4 :error/actual-count 2]
              [4 :error/attr :count-validator-test.nested/attr]
              [4 :error/entity 1]
              [4 :error/expected-count 3]
              [4 :error/type :count]]
             (->> (d/seek-datoms agg :eavt 4)
                  (map (juxt :e :a :v)))))))


(t/deftest permitted-attrs-validator
  (let [agg (-> (agg/allocate)
                (d/db-with [{:db/ident                                 :root
                             :permitted-attrs-validator-test/permitted true
                             :permitted-attrs-validator-test/rejected  true}
                            {:error/type   :some-error
                             :error/entity :root}])
                (agg/permitted-attrs-validator #{:permitted-attrs-validator-test/permitted}))]
    (t/is (= [[3 :error/attr :permitted-attrs-validator-test/rejected]
              [3 :error/entity 1]
              [3 :error/type :rejected]
              [3 :error/value true]]
             (->> (d/seek-datoms agg :eavt 3)
                  (map (juxt :e :a :v)))))))
