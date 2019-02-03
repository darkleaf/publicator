(ns publicator-ext.domain.util.validation-test
  (:require
   [publicator-ext.domain.util.validation :as sut]
   [datascript.core :as d]
   [clojure.test :as t]))

(defn- errors->set [errors]
  (->> errors
       (d/q '{:find [(pull ?e [*])]
              :where [[?e _ _]]})
       (flatten)
       (set)))

(defn- testing-attrs [name checks aggregate expected]
  (t/testing name
    (let [entities-q '{:find  [[?e ...]]
                       :where [[?e _ _]]}
          errors     (-> (sut/begin aggregate)
                         (sut/attributes entities-q checks)
                         (sut/end)
                         (errors->set))]
      (t/is (= expected errors)))))

(defn- testing-attrs-common [kind]
  (t/testing kind
    (testing-attrs "with empty aggregate"
                   [[kind :attr int?]]
                   (d/empty-db)
                   #{})
    (testing-attrs "with correct attribute"
                   [[kind :attr int?]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :attr 0]]))
                   #{})
    (testing-attrs "with wrong attribute"
                   [[kind :attr int?]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :attr :wrong-value]]))
                   #{{:db/id     1
                      :entity    42
                      :attribute :attr
                      :value     :wrong-value
                      :type      ::sut/predicate
                      :predicate int?
                      :args      []}})
    (testing-attrs "with predicate args"
                   [[kind :attr = :val]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :attr :val]]))
                   #{})
    (testing-attrs "with predicate args"
                   [[kind :attr = :val]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :attr :wrong-value]]))
                   #{{:db/id     1
                      :entity    42
                      :attribute :attr
                      :value     :wrong-value
                      :type      ::sut/predicate
                      :predicate =
                      :args      [:val]}})
    (testing-attrs "double"
                   [[kind :attr int?]
                    [kind :attr int?]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :attr :wrong-value]]))
                   #{{:db/id     1
                      :entity    42
                      :attribute :attr
                      :value     :wrong-value
                      :type      ::sut/predicate
                      :predicate int?
                      :args      []}})
    (testing-attrs "same attr"
                   [[kind :attr int?]
                    [kind :attr < 10]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :attr :wrong-value]]))
                   #{{:db/id     1
                      :entity    42
                      :attribute :attr
                      :value     :wrong-value
                      :type      ::sut/predicate
                      :predicate int?
                      :args      []}})))

(t/deftest attributes
  (testing-attrs-common :opt)
  (testing-attrs-common :req)
  (t/testing :opt
    (testing-attrs "with missing attribute"
                   [[:opt :attr int?]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :other-attr :value]]))
                   #{}))
  (t/testing :req
    (testing-attrs "with missing attribute"
                   [[:req :attr int?]]
                   (-> (d/empty-db)
                       (d/db-with [[:db/add 42 :other-attr :value]]))
                   #{{:db/id     1
                      :entity    42
                      :attribute :attr
                      :type      ::sut/required}})))

(t/deftest query
  (let [aggregate (-> (d/empty-db {:base {:db/valueType :db.type/ref}})
                      (d/db-with [{:db/ident :root}
                                  {:base :root
                                   :val  1}
                                  {:base :root
                                   :val  1}]))
        errors    (-> (sut/begin aggregate)
                      (sut/query '{:find  [[?e ...]]
                                   :where [[?e :db/ident :root]]}
                                 '{:find  [(clojure.core/sort ?v) .]
                                   :in    [$ ?e]
                                   :with  [?nested]
                                   :where [[?nested :base ?e]
                                           [?nested :val  ?v]]}
                                 = [1 1 2])
                      (sut/end)
                      (errors->set))]
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
