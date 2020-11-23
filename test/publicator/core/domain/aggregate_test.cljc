(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(swap! agg/schema merge
       {:build-test/many {:db/cardinality :db.cardinality/many}})

(t/deftest build
  (let [agg (agg/build)]
    (t/is (some? agg))
    (t/is (-> agg :schema (contains? :build-test/many)))
    (t/is (= 1 (d/q '[:find ?e . :where [?e :db/ident :root]]
                    agg)))))


(t/deftest remove-errors
  (let [agg (-> (agg/build {:error/entity :root}))]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))


(swap! agg/schema merge
       {:predicate-validator-test/attr {:agg/predicate #{:ok}}})

(t/deftest predicate-validator
  (let [agg (-> (agg/build [:db/add 2 :predicate-validator-test/attr :wrong]
                           [:db/add 3 :predicate-validator-test/attr :ok])
                (agg/predicate-validator))]
    (t/is (= [[4 :error/attr :predicate-validator-test/attr]
              [4 :error/entity 2]
              [4 :error/type :predicate]
              [4 :error/value :wrong]]
             (->> (d/seek-datoms agg :eavt 4)
                  (map (juxt :e :a :v)))))))


(swap! agg/schema merge
       {:required-attrs-validator-test.nested/root   {:db/valueType :db.type/ref}
        :required-attrs-validator-test.nested/status {:db/index true}})

(t/deftest required-attrs-validator
  (let [agg (-> (agg/build {:db/ident                        :root
                            :required-attrs-validator-test/a :ok}
                           {:db/id                                     2
                            :required-attrs-validator-test/c           :ok
                            :required-attrs-validator-test.nested/root :root}
                           {:db/id                                     3
                            :required-attrs-validator-test/d           :ok
                            :required-attrs-validator-test.nested/root :root}
                           {:db/id                                       4
                            :required-attrs-validator-test/c             :ok
                            :required-attrs-validator-test/d             :ok
                            :required-attrs-validator-test.nested/root   :root
                            :required-attrs-validator-test.nested/status :ready})
                (agg/required-attrs-validator
                 {:root
                  [:required-attrs-validator-test/a :required-attrs-validator-test/b]

                  :required-attrs-validator-test.nested/_root
                  [:required-attrs-validator-test/c :required-attrs-validator-test/d]

                  [:required-attrs-validator-test.nested/status :ready]
                  [:required-attrs-validator-test/e]}))]
    (t/is (= [[5 :error/attr :required-attrs-validator-test/b]
              [5 :error/entity 1]
              [5 :error/type :required]

              [6 :error/attr :required-attrs-validator-test/d]
              [6 :error/entity 2]
              [6 :error/type :required]

              [7 :error/attr :required-attrs-validator-test/c]
              [7 :error/entity 3]
              [7 :error/type :required]

              [8 :error/attr :required-attrs-validator-test/e]
              [8 :error/entity 4]
              [8 :error/type :required]]
             (->> (d/seek-datoms agg :eavt 5)
                  (map (juxt :e :a :v)))))))

(t/deftest permitted-attrs-validator
  (let [agg (-> (agg/build {:db/ident                                 :root
                            :permitted-attrs-validator-test/permitted true
                            :permitted-attrs-validator-test/rejected  true}
                           {:error/type   :some-error
                            :error/entity :root})
                (agg/permitted-attrs-validator #{:permitted-attrs-validator-test/permitted}))]
    (t/is (= [[3 :error/attr :permitted-attrs-validator-test/rejected]
              [3 :error/entity 1]
              [3 :error/type :rejected]
              [3 :error/value true]]
             (->> (d/seek-datoms agg :eavt 3)
                  (map (juxt :e :a :v)))))))


(t/deftest include?
  (let [agg (agg/build {:db/ident        :root
                        :include?/attr-1 :val-1
                        :include?/attr-2 :val-2})]
    (t/is (= true  (agg/include? agg :root :include?/attr-1 :val-1)))
    (t/is (= true  (agg/include? agg :root :include?/attr-1)))
    (t/is (= false (agg/include? agg :root :include?/attr-1 :val-2)))
    (t/is (= false (agg/include? agg :root :include?/attr-3)))))
