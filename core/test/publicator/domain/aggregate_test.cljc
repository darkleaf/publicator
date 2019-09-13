(ns publicator.domain.aggregate-test
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]
   [clojure.test :as t]))

(def agg (-> agg/blank
             (with-meta {:type :agg/test-agg})))

(defn rules-decorator [super agg]
  (conj (super agg)
        '[(attr ?v)
          [:root :test-agg/attr ?v]]))
(md/decorate agg/rules :agg/test-agg #'rules-decorator)

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/required-validator 'root #{:test-agg/attr})
      (agg/predicate-validator 'root {:test-agg/attr #'int?})
      (agg/query-validator 'root
                           '[:find ?v .
                             :where [?e :test-agg/attr2 ?v]]
                           #'int?)))
(md/decorate agg/validate :agg/test-agg #'validate-decorator)

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

(t/deftest q
  (t/testing "rules"
    (let [agg (agg/with agg [[:db/add :root :test-agg/attr :foo]])]
      (t/is (= :foo (agg/q agg '[:find ?v . :where (attr ?v)])))))
  (t/testing "bindings"
    (let [agg (agg/with agg [[:db/add :root :test-agg/attr :foo]])]
      (t/is (= :foo (agg/q agg
                           '[:find ?v .
                             :in ?attr
                             :where [:root ?attr ?v]]
                           :test-agg/attr))))))

(t/deftest validate
  (t/testing "required"
    (let [agg (-> agg
                  (agg/validate))]
      (t/is (= #{{:error/entity 1
                  :error/attr   :test-agg/attr
                  :error/rule   'root
                  :error/type   :required}}
               (agg/errors agg)))))
  (t/testing "predicate"
    (let [agg (-> agg
                  (agg/with [[:db/add :root :test-agg/attr :wrong]])
                  (agg/validate))]
      (t/is (= #{{:error/entity 1
                  :error/attr   :test-agg/attr
                  :error/value  :wrong
                  :error/pred   `int?
                  :error/rule   'root
                  :error/type   :predicate}}
               (agg/errors agg)))))
  (t/testing "query"
    (let [agg (-> agg
                  (agg/with [[:db/add :root :test-agg/attr 1]])
                  (agg/validate))]
      (t/is (= #{{:error/entity 1
                  :error/rule   'root
                  :error/pred   `int?
                  :error/query  '{:find [?v .], :where [[?e :test-agg/attr2 ?v]], :in [?e]}
                  :error/type   :query}}
               (agg/errors agg))))))
