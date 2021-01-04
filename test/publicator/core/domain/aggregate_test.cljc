(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest build
  (let [schema {:test/attr {:db/cardinality :db.cardinality/many}}
        build  (agg/->build schema)
        agg    (build)]
    (t/is (some? agg))
    (t/is (-> agg :schema (contains? :test/attr)))
    (t/is (= 1 (d/q '[:find ?e . :where [?e :db/ident :root]]
                    agg)))))

(t/deftest remove-errors
  (let [build (agg/->build)
        agg   (build [{:error/entity :root}])]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))

(t/deftest predicate-validator
  (let [schema {:test/attr {:agg/predicate #{:ok}}}
        build  (agg/->build schema)
        agg    (-> (build [[:db/add 2 :test/attr :wrong]
                           [:db/add 3 :test/attr :ok]])
                   (agg/abstract-validate))]
    (t/is (= [(d/datom 4 :error/attr :test/attr)
              (d/datom 4 :error/entity 2)
              (d/datom 4 :error/type :predicate)
              (d/datom 4 :error/value :wrong)]
             (d/seek-datoms agg :eavt 4)))))

(t/deftest predicate-validator-idempotence
  (let [schema {:test/attr {:agg/predicate #{:ok}}}
        build  (agg/->build schema)
        agg    (-> (build [[:db/add 2 :test/attr :wrong]
                           [:db/add 3 :test/attr :ok]])
                   (agg/abstract-validate)
                   (agg/abstract-validate))]
    (t/is (= [(d/datom 4 :error/attr :test/attr)
              (d/datom 4 :error/entity 2)
              (d/datom 4 :error/type :predicate)
              (d/datom 4 :error/value :wrong)]
             (d/seek-datoms agg :eavt 4)))))

(t/deftest required-attrs-validator
  (let [schema {:test/root   {:db/valueType :db.type/ref}
                :test/status {:db/index true}}
        build  (agg/->build schema)
        agg    (-> (build [{:db/ident :root
                            :test/a   :test-value}
                           {:db/id     2
                            :test/root :root
                            :test/c    :test-value}
                           {:db/id     3
                            :test/root :root
                            :test/d    :test-value}
                           {:db/id       4
                            :test/root   :root
                            :test/c      :test-value
                            :test/d      :test-value
                            :test/status :ready}])
                   (agg/abstract-validate)
                   (agg/required-attrs-validator {:root                 #{:test/a :test/b}
                                                  :test/_root           #{:test/c :test/d}
                                                  [:test/status :ready] #{:test/e}}))]
    (t/is (= [(d/datom 5 :error/attr :test/b)
              (d/datom 5 :error/entity 1)
              (d/datom 5 :error/type :required)

              (d/datom 6 :error/attr :test/d)
              (d/datom 6 :error/entity 2)
              (d/datom 6 :error/type :required)

              (d/datom 7 :error/attr :test/c)
              (d/datom 7 :error/entity 3)
              (d/datom 7 :error/type :required)

              (d/datom 8 :error/attr :test/e)
              (d/datom 8 :error/entity 4)
              (d/datom 8 :error/type :required)]
             (d/seek-datoms agg :eavt 5)))))

(t/deftest permitted-attrs-validator
  (let [build (agg/->build)
        agg   (-> (build [{:db/ident       :root
                           :test/permitted :test-value
                           :test/rejected  :test-value}
                          {:error/type   :test-error
                           :error/entity :root}])
                (agg/abstract-validate)
                (agg/permitted-attrs-validator #{:test/permitted}))]
    (t/is (= [(d/datom 3 :error/attr :test/rejected)
              (d/datom 3 :error/entity 1)
              (d/datom 3 :error/type :rejected)
              (d/datom 3 :error/value :test-value)]
             (d/seek-datoms agg :eavt 3)))))

(t/deftest include?
  (let [build (agg/->build)
        agg   (build [{:db/ident    :root
                       :test/attr-1 :value-1
                       :test/attr-2 :value-2}])]
    (t/is (= true  (agg/include? agg :root :test/attr-1 :value-1)))
    (t/is (= true  (agg/include? agg :root :test/attr-1)))
    (t/is (= false (agg/include? agg :root :test/attr-1 :value-2)))
    (t/is (= false (agg/include? agg :root :test/attr-3)))))
