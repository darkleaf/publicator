(ns publicator-ext.domain.abstractions.aggregate-test
  (:require
   [publicator-ext.domain.abstractions.aggregate :as sut]
   [publicator-ext.utils.test.instrument :as instrument]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]))

(t/use-fixtures :once instrument/fixture)

(s/def ::key keyword?)
(s/def ::root (s/keys :req [::key]))

(s/def ::inner-key keyword?)
(s/def ::inner-entity (s/keys :req [::inner-key]))

(def ^:const +schema+ {:inner/base {:db/valueType :db.type/ref}})

(t/deftest build-aggregate
  (let [id        1
        aggregate (sut/build +schema+ {:aggregate/id id
                                       :entity/type  ::root
                                       ::key         :val
                                       :inner/_base  [{::inner-key  :inner-val
                                                       :entity/type ::inner-entity}]})]
    (t/testing "root"
      (t/is (= :inner-val (-> aggregate sut/root :inner/_base first ::inner-key))))
    (t/testing "id"
      (t/is (= id (sut/id aggregate))))
    (t/testing "type"
      (t/is (= ::root (sut/type aggregate))))))

(t/deftest update-aggregate
  (let [aggregate (sut/build +schema+ {:aggregate/id 1
                                       :entity/type  ::root
                                       ::key         :val
                                       :inner/_base  [{::inner-key  :inner-val
                                                       :entity/type ::inner-entity}]})
        aggregate (sut/update aggregate [[:db/add :root ::key :new-val]])]
    (t/is (= :new-val (-> aggregate sut/root ::key)))))
