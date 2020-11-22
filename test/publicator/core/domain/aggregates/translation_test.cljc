(ns publicator.core.domain.aggregates.translation-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest full-translation-validator-test
  (let [agg (-> (agg/build {:translation/root :root
                            :translation/lang :en})
                (agg/validate)
                (translation/full-translation-validator))]
    (t/is (= [[3 :error/actual-langs #{:en}]
              [3 :error/entity 1]
              [3 :error/expected-langs translation/langs]
              [3 :error/type :full-translation]]
             (->> (d/seek-datoms agg :eavt 3)
                  (map (juxt :e :a :v)))))))
