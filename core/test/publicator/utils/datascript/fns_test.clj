(ns publicator.utils.datascript.fns-test
  (:require
   [publicator.utils.datascript.fns :as d.fns]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest update-all
  (let [schema   {:counters {:db/cardinality :db.cardinality/many}}
        actual   (-> (d/empty-db schema)
                     (d/db-with [[:db/add 1 :counters 1]
                                 [:db/add 1 :counters 2]
                                 [:db/add 2 :counters 10]
                                 [:db/add 2 :counters 20]])
                     (d/db-with [[:db.fn/call d.fns/update-all :counters inc]]))
        expected (-> (d/empty-db schema)
                     (d/db-with [[:db/add 1 :counters 2]
                                 [:db/add 1 :counters 3]
                                 [:db/add 2 :counters 11]
                                 [:db/add 2 :counters 21]]))]
    (t/is (= expected actual))))
