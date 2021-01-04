(ns publicator.core.domain.aggregates.translation-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest validate-test
  (let [build (agg/->build translation/schema)
        agg   (-> (build [{:translation/root :root
                           :translation/lang :en}])
                  (agg/abstract-validate)
                  (translation/validate :full-translation true))]
    (t/is (= [(d/datom 3 :error/entity 1)
              (d/datom 3 :error/missed-langs [:ru])
              (d/datom 3 :error/type :full-translation)]
             (d/seek-datoms agg :eavt 3)))))
