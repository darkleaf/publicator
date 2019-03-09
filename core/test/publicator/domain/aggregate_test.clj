(ns publicator.domain.aggregate-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.scaffolding :as scaffolding]
   [publicator.utils.datascript.fns :as d.fns]
   [publicator.utils.datascript.validation :as d.validation]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(def ^:const id 42)

(def spec
  {:type          :test-agg
   :schema        {:inner/base {:db/valueType :db.type/ref}}
   :on-build-tx   (fn [] [[:db/add :root :root/id id]
                          [:db/add :root :test-agg/version 0]
                          [:db/add :root :test-agg/read-only 0]])
   :additional-tx (fn [] [[:db.fn/call d.fns/update-all :test-agg/version inc]])
   :validator     (d.validation/attributes [:test-agg/key keyword?]
                                           [:test-agg/version pos-int?]
                                           [:teat-agg/read-only pos-int?]
                                           [:inner/key keyword?])
   :read-only     #{:test-agg/read-only}})

;; (t/deftest allocate
;;   (let [tx-data [[:db/add :root :root/id 1]]
;;         agg     (aggregate/allocate spec tx-data)]
;;     (t/is (some? agg))))

(t/deftest build
  (t/testing "main path"
    (let [agg (agg/build spec
                         [[:db/add :root :test-agg/key :val]
                          [:db/add "inner" :inner/base :root]
                          [:db/add "inner" :inner/key :inner-val]])]
      (t/testing "some"
        (t/is (some? agg)))
      (t/testing "id"
        (t/is (= id (-> agg agg/root :root/id))))
      (t/testing "type"
        (t/is (= :test-agg (type agg))))
      (t/testing :schema
        (t/is (= :inner-val (-> agg agg/root :inner/_base first :inner/key))))
      (t/testing :additional-tx
        (t/is (= 1 (-> agg agg/root :test-agg/version))))
      (t/testing :aggregate/changes
        (t/is (not-empty (-> agg meta :aggregate/changes))))
      (t/testing :aggregate/errors
        (t/is (empty? (-> agg meta :aggregate/errors))))))
  (t/testing "errors"
    (let [agg (agg/build spec
                         [[:db/add :root :test-agg/key "wrong"]])]
      (t/is (not-empty (-> agg meta :aggregate/errors))))))


;; (t/deftest build!)

;; (t/deftest change
;;   (let [aggregate (sut/build ::aggregate id
;;                              [{:db/ident :root
;;                                :root/key :val}
;;                               {:inner/base :root
;;                                :inner/key  :inner-val}])
;;         aggregate (sut/change aggregate [[:db/add :root :root/key :new-val]])]
;;     (t/is (= :new-val (-> aggregate sut/root :root/key)))))
