(ns publicator.domain.aggregate-test
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.test-impl.scaffolding :as scaffolding]
   [publicator.utils.datascript.fns :as d.fns]
   [clojure.test :as t]))

(t/use-fixtures :each scaffolding/setup)

(def ^:const id 42)

(def spec
  {:type         :test-agg
   :id-generator (constantly id)})

(t/deftest build
  (let [agg (agg/build spec)]
    (t/testing "some"
      (t/is (some? agg)))
    (t/testing "id"
      (t/is (= id (agg/id agg))))
    (t/testing "type"
      (t/is (= :test-agg (agg/type agg))))
    (t/testing "spec"
      (t/is (= spec (-> agg meta ::agg/spec))))))

(t/deftest change
  (let [agg (agg/build spec)]
    (t/testing "allow-everething"
      (let [agg (agg/change agg
                            [[:db/add :root :test-agg/key :val]]
                            agg/allow-everething)]
        (t/is (= :val (-> agg agg/root :test-agg/key)))))
    (t/testing "allow-attributes"
      (t/testing "allowed"
        (let [agg (agg/change agg
                              [[:db/add :root :test-agg/key :val]]
                              (agg/allow-attributes #{:test-agg/key}))]
          (t/is (= :val (-> agg agg/root :test-agg/key)))))
      (t/testing "rejected"
        (t/is (thrown? clojure.lang.ExceptionInfo
                       #"Wrong transaction"
                       (agg/change agg
                                   [[:db/add :root :wrong-attr :val]]
                                   (agg/allow-attributes #{:test-agg/key}))))))))
