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

(t/deftest assoc-in
  (let [inner-q  '{:find  [[?e ...]]
                   :where [[?e :inner/base :root]]}
        schema   {:inner/base {:db/valueType :db.type/ref}}
        actual   (-> (d/empty-db schema)
                     (d/db-with [[:db/add 1 :db/ident :root]
                                 [:db/add 2 :inner/base 1]
                                 [:db/add 3 :inner/base 1]])
                     (d/db-with [[:db.fn/call d.fns/assoc-in inner-q :attr :val]]))
        expected (-> (d/empty-db schema)
                     (d/db-with [[:db/add 1 :db/ident :root]
                                 [:db/add 2 :inner/base 1]
                                 [:db/add 3 :inner/base 1]

                                 [:db/add 2 :attr :val]
                                 [:db/add 3 :attr :val]]))]
    (t/is (= expected actual))))
