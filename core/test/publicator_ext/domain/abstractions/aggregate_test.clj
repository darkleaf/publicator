(ns publicator-ext.domain.abstractions.aggregate-test
  (:require
   [publicator-ext.domain.abstractions.aggregate :as sut]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.utils.test.instrument :as instrument]
   [clojure.test :as t]))

(t/use-fixtures :once instrument/fixture)

(defmethod sut/schema ::aggregate [_]
  {:inner/base {:db/valueType :db.type/ref}})

(defmethod sut/validator ::aggregate [chain]
  (-> chain
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:req :root/key keyword?]])
      (validation/attributes '[[(entity ?e)
                                [?e :inner/base :root]]]
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

;; (t/deftest change
;;   (let [aggregate (sut/build +schema+ {:aggregate/id 1
;;                                        :entity/type  ::root
;;                                        ::key         :val
;;                                        :inner/_base  [{::inner-key  :inner-val
;;                                                        :entity/type ::inner-entity}]})
;;         aggregate (sut/update aggregate [[:db/add :root ::key :new-val]])]
;;     (t/is (= :new-val (-> aggregate sut/root ::key)))))
