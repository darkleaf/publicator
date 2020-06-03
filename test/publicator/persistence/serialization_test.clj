(ns publicator.persistence.serialization-test
  (:require
   [clojure.test :as t]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.persistence.serialization :as sut]))

(swap! agg/schema merge
       {::ref  {:db/valueType :db.type/ref}
        ::tags {:db/cardinality :db.cardinality/many}})

(t/deftest ok
  (let [agg (-> (agg/build)
                (d/db-with [{:db/ident :root
                             ::a       "x"
                             ::tags    ["a" "b"]}
                            {::a    "x"
                             ::ref  :root
                             ::tags ["a" "c"]}
                            {::a    "y"
                             ::b    "y"
                             ::ref  :root
                             ::tags ["c" "d"]}]))
        row {"r:publicator.persistence.serialization-test/tags" ["a" "b"]
             "r:publicator.persistence.serialization-test/a"    "x"
             "e:publicator.persistence.serialization-test/a"    [2 3]
             "v:publicator.persistence.serialization-test/a"    ["x" "y"]
             "e:publicator.persistence.serialization-test/b"    [3]
             "v:publicator.persistence.serialization-test/b"    ["y"]
             "e:publicator.persistence.serialization-test/ref"  [2 3]
             "v:publicator.persistence.serialization-test/ref"  [1 1]
             "e:publicator.persistence.serialization-test/tags" [2 2 3 3]
             "v:publicator.persistence.serialization-test/tags" ["a" "c" "c" "d"]}]
    (t/is (= row (sut/agg->row agg)))
    (t/is (= agg (sut/row->agg row)))))
