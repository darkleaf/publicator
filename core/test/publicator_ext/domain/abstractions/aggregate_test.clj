(ns publicator-ext.domain.abstractions.aggregate-test
  (:require
   [publicator-ext.domain.abstractions.aggregate :as sut]
   [publicator-ext.domain.util.validation :as validation]
   [clojure.test :as t]))

(defmethod sut/schema ::aggregate [_]
  {:inner/base {:db/valueType :db.type/ref}})

(defmethod sut/validator ::aggregate [chain]
  (-> chain
      (validation/attributes '{:find [[?e ...]]
                               :where [[?e :db/ident :root]]}
                             [[:req :root/key keyword?]])
      (validation/attributes '{:find [[?e ...]]
                               :where [[?e :inner/base :root]]}
                             [[:req :inner/key keyword?]])))

(t/deftest build
  (let [id        1
        aggregate (sut/build ::aggregate id
                             [{:db/ident :root
                               :root/key :val}
                              {:inner/base :root
                               :inner/key  :inner-val}])]
    (t/testing "id"
      (t/is (= id (-> aggregate sut/root :aggregate/id))))
    (t/testing "type"
      (t/is (= ::aggregate (type aggregate))))
    (t/testing "root"
      (t/is (= :inner-val (-> aggregate sut/root :inner/_base first :inner/key))))))

(t/deftest change
  (let [aggregate (sut/build ::aggregate 1
                             [{:db/ident :root
                               :root/key :val}
                              {:inner/base :root
                               :inner/key  :inner-val}])
        aggregate (sut/change aggregate [[:db/add :root :root/key :new-val]])]
    (t/is (= :new-val (-> aggregate sut/root :root/key)))))
