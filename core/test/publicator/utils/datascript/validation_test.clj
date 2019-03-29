(ns publicator.utils.datascript.validation-test
  (:require
   [publicator.utils.datascript.validation :as d.validation]
   [datascript.core :as d]
   [clojure.test :as t]))

(defn- errors->set [errors]
  (->> errors
       (d/q '{:find [(pull ?e [*])]
              :where [[?e _ _]]})
       (flatten)
       (set)))

(defn- get-errors [db validator]
  (let [errors  (d.validation/validate db validator)]
    (errors->set errors)))

(t/deftest predicate
  (let [validator (d.validation/predicate [[:attr int?]
                                           [:attr < 10]])]
    (t/testing "missed"
      (let [db     (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :other :val]]))
            errors (get-errors db validator)]
        (t/is (= #{} errors))))
    (t/testing "correct"
      (let [db     (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :attr 0]]))
            errors (get-errors db validator)]
        (t/is (= #{} errors))))
    (t/testing "first check"
      (let [db     (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :attr :wrong]]))
            errors (get-errors db validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :value     :wrong
                    :predicate int?
                    :args      []
                    :type      ::d.validation/predicate}}
                 errors))))
    (t/testing "second check"
      (let [db     (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :attr 10]]))
            errors (get-errors db validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :value     10
                    :predicate <
                    :args      [10]
                    :type      ::d.validation/predicate}}
                 errors))))
    (t/testing "many"
      (let [db     (-> (d/empty-db {:attr {:db/cardinality :db.cardinality/many}})
                       (d/db-with [[:db/add 1 :attr 1]
                                   [:db/add 1 :attr 10]]))
            errors (get-errors db validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :value     10
                    :predicate <
                    :args      [10]
                    :type      ::d.validation/predicate}}
                 errors))))
    (t/testing "idempotence"
      (let [db        (-> (d/empty-db)
                          (d/db-with [[:db/add 1 :attr :wrong]]))
            validator (d.validation/compose validator validator)
            errors    (get-errors db validator)]
        (t/is (= 1 (count errors)))))))

(t/deftest required
  (let [validator (d.validation/required #{:attr})]
    (t/testing "empty"
      (let [db     (-> (d/empty-db)
                       (d/db-with []))
            errors (get-errors db validator)]
        (t/is (= #{} errors))))
    (t/testing "missing"
      (let [db     (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :other :val]]))
            errors (get-errors db validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :type      ::d.validation/required}}
                 errors))))
    (t/testing "idempotence"
      (let [db        (-> (d/empty-db)
                          (d/db-with [[:db/add 1 :other :val]]))
            validator (d.validation/compose validator validator)
            errors    (get-errors db validator)]
        (t/is (= 1 (count errors)))))))

(t/deftest query
  (let [entities-q '{:find  [[?e ...]]
                     :where [[?e :db/ident :root]]}
        value-q    '{:find  [(clojure.core/sort ?v) .]
                     :in    [$ ?e]
                     :with  [?nested]
                     :where [[?nested :base ?e]
                             [?nested :val  ?v]]}
        validator  (d.validation/query entities-q
                                       value-q
                                       = [1 1 2])
        db         (-> (d/empty-db {:base {:db/valueType :db.type/ref}})
                       (d/db-with [{:db/ident :root}
                                   {:base :root
                                    :val  1}
                                   {:base :root
                                    :val  1}]))
        errors     (get-errors db validator)]
    (t/is (= #{{:db/id     1
                :entity    1
                :value     [1 1]
                :value-q   value-q
                :predicate =
                :args      [[1 1 2]]
                :type      ::d.validation/query}}
             errors))))
