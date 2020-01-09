(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]
   [clojure.test :as t]))

(md/decorate agg/rules :agg/test-agg
  (fn [super type]
    (conj (super type)
          '[(attr ?v)
            [:root :test-agg/attr ?v]])))

(md/decorate agg/validate :agg/test-agg
  (fn [super agg]
    (-> (super agg)
        (agg/required-validator 'root
          #{:test-agg/attr})
        (agg/predicate-validator 'root
          {:test-agg/attr #'int?})
        (agg/query-validator 'root
          '[:find ?v .
            :where [?e :test-agg/attr2 ?v]]
          #'int?))))

(md/decorate agg/schema :agg/test-agg
  (fn [super type]
    (assoc (super type)
           :test-agg/many {:db/cardinality :db.cardinality/many})))

(t/deftest allocate
  (let [agg (agg/allocate :agg/test-agg)]
    (t/is (some? agg))
    (t/is (not (agg/has-errors? agg)))
    (t/is (some? (agg/root agg)))
    (t/is (= 1 (agg/q agg '[:find ?e . :where (root ?e)])))
    (t/is (-> agg :schema (contains? :test-agg/many)))))

(t/deftest q
  (let [agg (-> (agg/allocate :agg/test-agg)
                (agg/apply-tx [[:db/add :root :test-agg/attr :foo]]))]
    (t/testing "rules"
      (t/is (= :foo (agg/q agg '[:find ?v . :where (attr ?v)]))))
    (t/testing "bindings"
      (t/is (= :foo (agg/q agg
                           '[:find ?v .
                             :in ?attr
                             :where [:root ?attr ?v]]
                           :test-agg/attr))))))

(t/deftest validate
  (let [agg (agg/allocate :agg/test-agg)]
    (t/testing "required"
      (t/is (= (agg/apply-tx agg [{:error/rule   'root
                                   :error/entity :root
                                   :error/attr   :test-agg/attr
                                   :error/type   :required}])
               (agg/validate agg))))
    (t/testing "predicate"
      (let [agg (-> agg
                    (agg/apply-tx [[:db/add :root :test-agg/attr :wrong]]))]
        (t/is (= (agg/apply-tx agg [{:error/rule   'root
                                     :error/entity :root
                                     :error/attr   :test-agg/attr
                                     :error/value  :wrong
                                     :error/pred   `int?
                                     :error/type   :predicate}])
                 (agg/validate agg)))))
    (t/testing "query"
      (let [agg (-> agg
                    (agg/apply-tx [[:db/add :root :test-agg/attr 1]]))]
        (t/is (= (agg/apply-tx agg [{:error/rule   'root
                                     :error/entity :root
                                     :error/pred   `int?
                                     :error/query  '[:find ?v .
                                                     :where [?e :test-agg/attr2 ?v]]
                                     :error/type   :query}])
                 (agg/validate agg)))))
    (t/testing "clear previous errors"
      (let [agg (-> agg
                    (agg/validate)
                    (agg/apply-tx [[:db/add :root :test-agg/attr :wrong]]))]
        (t/is (= (agg/apply-tx agg [[:db/retractEntity 2]
                                    {:error/rule   'root
                                     :error/entity :root
                                     :error/attr   :test-agg/attr
                                     :error/value  :wrong
                                     :error/pred   `int?
                                     :error/type   :predicate}])
                 (agg/validate agg)))))))
