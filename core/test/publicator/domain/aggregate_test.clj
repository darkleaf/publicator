(ns publicator.domain.aggregate-test
  (:require
   [publicator.domain.aggregate :as agg]
   [clojure.test :as t]))

(derive :test-agg/attr :agg/add-attribute)

(t/deftest blank
  (t/is (some? agg/blank))
  (t/is (some? (agg/root agg/blank)))
  (t/is (= 1 (agg/q agg/blank '{:find [?e .], :where [(root ?e)]}))))

(t/deftest extend-schema
  (let [agg (-> agg/blank
                (agg/extend-schema {:test-agg/many {:db/cardinality :db.cardinality/many}}))]
    (t/is (some? (agg/root agg)))
    (t/is (-> agg :schema (contains? :test-agg/many)))))

(t/deftest decorate
  (let [agg (agg/decorate agg/blank
                          {::agg/rules (fn [rules]
                                         (conj rules
                                               '[(attr ?v)
                                                 [:root :test-agg/attr ?v]]))})]
    (t/is (= 'attr (-> agg agg/rules last first first)))))

(t/deftest reducer
  (let [agg (agg/reducer agg/blank [:test-agg/attr {:entity :root, :value :foo}])]
    (t/is (= :foo (-> agg agg/root :test-agg/attr)))))

(t/deftest q
  (t/testing "rules"
    (let [agg (-> agg/blank
                  (agg/decorate {::agg/rules (fn [rules]
                                               (conj rules
                                                     '[(attr ?v)
                                                       [:root :test-agg/attr ?v]]))})
                  (agg/reducer [:test-agg/attr {:entity :root, :value :foo}]))]
      (t/is (= :foo (agg/q agg '{:find [?v .]
                                 :where [(attr ?v)]})))))
  (t/testing "bindings"
    (let [agg (-> agg/blank
                  (agg/reducer [:test-agg/attr {:entity :root, :value :foo}]))]
      (t/is (= :foo (agg/q agg
                           '{:find  [?v .]
                             :in [?attr]
                             :where [[:root ?attr ?v]]}
                           [:test-agg/attr]))))))
