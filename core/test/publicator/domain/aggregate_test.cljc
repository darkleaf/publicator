(ns publicator.domain.aggregate-test
  (:require
   [publicator.domain.aggregate :as agg]
   [clojure.test :as t]))

(t/deftest blank
  (t/is (some? agg/blank))
  (t/is (not (agg/has-errors? agg/blank)))
  (t/is (some? (agg/root agg/blank)))
  (t/is (= 1 (agg/q agg/blank '[:find ?e . :where (root ?e)]))))

(t/deftest extend-schema
  (let [agg (-> agg/blank
                (agg/extend-schema {:test-agg/many {:db/cardinality :db.cardinality/many}}))]
    (t/is (some? (agg/root agg)))
    (t/is (-> agg :schema (contains? :test-agg/many)))
    (t/is (= (meta agg/blank)
             (meta agg)))))

(t/deftest decorate
  (let [decorators {`agg/rules (fn [super agg]
                                 (conj (super agg)
                                       '[(attr ?v)
                                         [:root :test-agg/attr ?v]]))}
        agg        (agg/decorate agg/blank decorators)]
    (t/is (= 'attr (-> agg agg/rules last first first)))))

(t/deftest q
  (t/testing "rules"
    (let [rules-d    (fn [super agg]
                       (conj (super agg)
                             '[(attr ?v)
                               [:root :test-agg/attr ?v]]))
          decorators {`agg/rules rules-d}
          agg        (-> agg/blank
                         (agg/decorate decorators)
                         (agg/with [[:db/add :root :test-agg/attr :foo]]))]
      (t/is (= :foo (agg/q agg '[:find ?v . :where (attr ?v)])))))
  (t/testing "bindings"
    (let [agg (agg/with agg/blank [[:db/add :root :test-agg/attr :foo]])]
      (t/is (= :foo (agg/q agg
                           '[:find ?v .
                             :in ?attr
                             :where [:root ?attr ?v]]
                           :test-agg/attr))))))

(t/deftest apply-msgs
  (let [msgs [[:add-attr :root :test-agg/attr :foo]]
        agg (agg/apply-msgs agg/blank msgs)]
    (t/is (= :foo (-> agg agg/root :test-agg/attr)))))

(t/deftest validate
  (t/testing "generic"
    (let [validate-d (fn [super agg]
                       (-> (super agg)
                           (agg/with [{:error/enitiy 1
                                       :error/type   :generic}])))
          decorators {`agg/validate validate-d}
          agg        (-> agg/blank
                         (agg/decorate decorators)
                         (agg/validate))]
      (t/is (agg/has-errors? agg))))
  (t/testing "required"
    (let [validate-d (fn [super agg]
                       (-> (super agg)
                           (agg/required-validator 'root #{:test-att/attr})))
          decorators {`agg/validate validate-d}
          agg        (-> agg/blank
                         (agg/decorate decorators)
                         (agg/validate))]
      (t/is (agg/has-errors? agg))))
  (t/testing "predicate"
    (let [validate-d (fn [super agg]
                       (-> (super agg)
                           (agg/predicate-validator 'root {:test-agg/attr int?})))
          decorators {`agg/validate validate-d}
          agg        (-> agg/blank
                         (agg/with [[:db/add :root :test-agg/attr :wrong]])
                         (agg/decorate decorators)
                         (agg/validate))]
      (t/is (agg/has-errors? agg))))
  (t/testing "query"
    (let [validate-d (fn [super agg]
                       (-> (super agg)
                           (agg/query-validator
                            'root
                            '[:find ?v .
                              :where [?e :test-agg/attr ?v]]
                            int?)))
          decorators {`agg/validate validate-d}
          agg        (-> agg/blank
                         (agg/with [[:db/add :root :test-agg/attr :wrong]])
                         (agg/decorate decorators)
                         (agg/validate))]
      (t/is (agg/has-errors? agg)))))
