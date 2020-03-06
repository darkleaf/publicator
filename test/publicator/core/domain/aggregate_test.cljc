(ns publicator.core.domain.aggregate-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]
   [datascript.core :as d]
   [clojure.test :as t]))

(md/decorate agg/schema :allocate/agg
  (fn [super type]
    (assoc (super type)
           :allocate/many {:db/cardinality :db.cardinality/many})))

(t/deftest allocate
  (let [agg (agg/allocate :allocate/agg)]
    (t/is (some? agg))
    (t/is (= :allocate/agg (u/type agg)))
    (t/is (-> agg :schema (contains? :allocate/many)))
    (t/is (= 1 (d/q '[:find ?e . :where [?e :db/ident :root]]
                    agg)))))


(t/deftest remove-errors
  (let [agg (-> (agg/allocate :remove-errors/agg)
                (d/db-with [{:error/entity :root}]))]
    (t/is (-> agg agg/has-errors?))
    (t/is (-> agg agg/remove-errors agg/has-no-errors?))))


(md/decorate agg/validate :validate/agg
  (fn [super agg]
    (-> (super agg)
        (d/db-with [{:error/entity :root}]))))

(t/deftest validate
  (let [agg (-> (agg/allocate :validate/agg)
                (agg/validate)
                (agg/validate))]
    (t/is (= [[1 :db/ident :root]
              #_[2 :error/entity 1]
              [3 :error/entity 1]]
             (->> (d/datoms agg :eavt)
                  (map (juxt :e :a :v)))))))


(t/deftest predicate-validator
  (let [agg (-> (agg/allocate :predicate-validator/agg)
                (d/db-with [[:db/add 2 :predicate-validator/attr :wrong]
                            [:db/add 3 :predicate-validator/attr 0]])
                (agg/predicate-validator {:predicate-validator/attr #'int?}))]
    (t/is (= [[4 :error/attr :predicate-validator/attr]
              [4 :error/entity 2]
              [4 :error/pred `int?]
              [4 :error/type :predicate]
              [4 :error/value :wrong]]
             (->> (d/seek-datoms agg :eavt 4)
                  (map (juxt :e :a :v)))))))


(md/decorate agg/schema :required-validator/agg
  (fn [super type]
    (assoc (super type)
           :required-validator.nested/root {:db/valueType :db.type/ref})))

(t/deftest required-validator
  (let [agg (-> (agg/allocate :required-validator/agg)
                (d/db-with [{:db/ident :root
                             :a        :ok}
                            {:db/id                          2
                             :c                              :ok
                             :required-validator.nested/root :root}
                            {:db/id                          3
                             :d                              :ok
                             :required-validator.nested/root :root}])
                (agg/required-validator {:root                            [:a :b]
                                         :required-validator.nested/_root [:c :d]}))]
    (t/is (= [[4 :error/attr :b]
              [4 :error/entity 1]
              [4 :error/type :required]

              [5 :error/attr :c]
              [5 :error/entity 3]
              [5 :error/type :required]

              [6 :error/attr :d]
              [6 :error/entity 2]
              [6 :error/type :required]]
             (->> (d/seek-datoms agg :eavt 4)
                  (map (juxt :e :a :v)))))))
