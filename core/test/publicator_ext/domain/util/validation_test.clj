(ns publicator-ext.domain.util.validation-test
  (:require
   [publicator-ext.domain.util.validation :as sut]
   [publicator-ext.utils.test.instrument :as instrument]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(defn- errors->set [errors]
  (->> errors
       (d/q '{:find [(pull ?e [*])]
              :where [[?e _ _]]})
       (flatten)
       (set)))

(defn- testing-attrs [name checks aggregate expected]
  (t/testing name
    (let [errors (-> (sut/begin aggregate)
                     (sut/attributes '[[(entity ?e)
                                        [?e _ _]]]
                                     checks)
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
