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
   :defaults-tx   (fn [] [[:db/add :root :root/id id]
                          [:db/add :root :test-agg/version 0]
                          [:db/add :root :test-agg/read-only 0]])
   :additional-tx (fn [] [[:db.fn/call d.fns/update-all :test-agg/version inc]])
   :validator     (d.validation/compose

                   (d.validation/predicate
                    [[:test-agg/key keyword?]
                     [:test-agg/version pos-int?]
                     [:teat-agg/read-only pos-int?]
                     [:inner/key keyword?]])

                   (d.validation/read-only #{:test-agg/read-only}))})

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
      (t/testing :aggregate/tx-data
        (t/is (not-empty (-> agg meta :aggregate/tx-data))))
      (t/testing :aggregate/errors
        (t/is (empty? (-> agg meta :aggregate/errors))))))
  (t/testing "errors"
    (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Aggregate has errors"
                            (agg/build spec [[:db/add :root :test-agg/key "wrong"]])))))

(t/deftest change
  (let [agg (agg/build spec
                       [[:db/add :root :test-agg/key :val]])]
    (t/testing "main path"
      (let [agg (agg/change agg [[:db/add :root :test-agg/key :new-val]])]
        (t/testing "update"
          (t/is (= :new-val (-> agg agg/root :test-agg/key))))
        (t/testing :additional-tx
          (t/is (= 2 (-> agg agg/root :test-agg/version))))
        (t/testing :aggregate/tx-data
          (t/is (= [[:test-agg/key false]
                    [:test-agg/key true]
                    [:root/updated-at false]
                    [:root/updated-at true]
                    [:test-agg/version false]
                    [:test-agg/version true]]
                   (map (fn [[e a v t added]] [a added])
                        (-> agg meta :aggregate/tx-data)))))
        (t/testing :aggregate/errors
          (t/is (empty? (-> agg meta :aggregate/errors))))))
    (t/testing "errors"
      (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                              #"Aggregate has errors"
                              (agg/change agg [[:db/add :root :test-agg/key "wrong"]]))))))
