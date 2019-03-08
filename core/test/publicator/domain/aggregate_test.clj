(ns publicator.domain.aggregate-test
  (:require
   [publicator.domain.aggregate :as sut]
   [publicator.domain.abstractions.scaffolding :as scaffolding]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(defmethod sut/schema ::aggregate [_]
  {:inner/base {:db/valueType :db.type/ref}})

(def ^:const id 42)

(t/deftest build
  (let [aggregate (sut/build ::aggregate id
                             [{:db/ident :root
                               :root/key :val}
                              {:inner/base :root
                               :inner/key  :inner-val}])]
    (t/testing "id"
      (t/is (= id (-> aggregate sut/root :root/id))))
    (t/testing "type"
      (t/is (= ::aggregate (type aggregate))))
    (t/testing "root"
      (t/is (= :inner-val (-> aggregate sut/root :inner/_base first :inner/key))))))

(t/deftest change
  (let [aggregate (sut/build ::aggregate id
                             [{:db/ident :root
                               :root/key :val}
                              {:inner/base :root
                               :inner/key  :inner-val}])
        aggregate (sut/change aggregate [[:db/add :root :root/key :new-val]])]
    (t/is (= :new-val (-> aggregate sut/root :root/key)))))
