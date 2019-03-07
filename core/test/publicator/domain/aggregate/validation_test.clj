(ns publicator.domain.aggregate.validation-test
  (:require
   [publicator.domain.aggregate.validation :as sut]
   [datascript.core :as d]
   [clojure.test :as t]))

(defn- errors->set [errors]
  (->> errors
       (d/q '{:find [(pull ?e [*])]
              :where [[?e _ _]]})
       (flatten)
       (set)))

(defn- get-errors [aggregate validator]
  (-> aggregate
      (validator)
      (meta)
      :aggregate/errors
      (errors->set)))

(t/deftest types
  (let [validator #(sut/types %
                              [:attr int?]
                              [:attr < 10])]
    (t/testing "empty"
      (let [agg    (d/empty-db)
            errors (get-errors agg validator)]
        (t/is (= #{} errors))))
    (t/testing "missed"
      (let [agg    (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :other :val]]))
            errors (get-errors agg validator)]
        (t/is (= #{} errors))))
    (t/testing "correct"
      (let [agg    (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :attr 0]]))
            errors (get-errors agg validator)]
        (t/is (= #{} errors))))
    (t/testing "first check"
      (let [agg    (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :attr :wrong]]))
            errors (get-errors agg validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :value     :wrong
                    :predicate int?
                    :args      []
                    :type      ::sut/predicate}}
                 errors))))
    (t/testing "second check"
      (let [agg    (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :attr 10]]))
            errors (get-errors agg validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :value     10
                    :predicate <
                    :args      [10]
                    :type      ::sut/predicate}}
                 errors))))
    (t/testing "many"
      (let [agg    (-> (d/empty-db {:attr {:db/cardinality :db.cardinality/many}})
                       (d/db-with [[:db/add 1 :attr 1]
                                   [:db/add 1 :attr 10]]))
            errors (get-errors agg validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :value     10
                    :predicate <
                    :args      [10]
                    :type      ::sut/predicate}}
                 errors))))))

(t/deftest required-for
  (let [validator #(-> %
                       (sut/types [:attr int?])
                       (sut/required-for '{:find  [[?e ...]]
                                           :where [[?e :type :active]]}
                                         [:attr = 0]))]
    (t/testing "empty"
      (let [agg    (d/empty-db)
            errors (get-errors agg validator)]
        (t/is (= #{} errors))))
    (t/testing "missing"
      (let [agg    (-> (d/empty-db)
                       (d/db-with [[:db/add 1 :type :active]]))
            errors (get-errors agg validator)]
        (t/is (= #{{:db/id     1
                    :entity    1
                    :attribute :attr
                    :type      ::sut/required}}
                 errors))))
    (t/testing "scope"
      (let [agg    (-> (d/empty-db)
                       (d/db-with [{:db/id 1
                                    :type  :active
                                    :attr  0}
                                   {:db/id 2
                                    :type  :inactive}]))
            errors (get-errors agg validator)]
        (t/is (= #{} errors))))))

(t/deftest query
  (let [validator #(sut/query %
                              '{:find  [[?e ...]]
                                :where [[?e :db/ident :root]]}
                              '{:find  [(clojure.core/sort ?v) .]
                                :in    [$ ?e]
                                :with  [?nested]
                                :where [[?nested :base ?e]
                                        [?nested :val  ?v]]}
                              = [1 1 2])
        aggregate (-> (d/empty-db {:base {:db/valueType :db.type/ref}})
                      (d/db-with [{:db/ident :root}
                                  {:base :root
                                   :val  1}
                                  {:base :root
                                   :val  1}]))
        errors    (get-errors aggregate validator)]
    (t/is (= #{{:db/id     1
                :entity    1
                :value     [1 1]
                :query     '{:find  [(clojure.core/sort ?v) .]
                             :in    [$ ?e]
                             :with  [?nested]
                             :where [[?nested :base ?e]
                                     [?nested :val  ?v]]}
                :predicate =
                :args      [[1 1 2]]
                :type      ::sut/query}}
             errors))))
