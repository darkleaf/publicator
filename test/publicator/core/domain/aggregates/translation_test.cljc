(ns publicator.core.domain.aggregates.translation-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest validate-test
  (let [validators (-> agg/proto-validators
                       (translation/validators-mixin)
                       (d/db-with [[:translation.full/upsert]]))
        agg        (-> agg/proto-agg
                       (translation/agg-mixin)
                       (d/db-with [{:translation/entity :root
                                    :translation/lang   :en}
                                   {:translation/entity :root
                                    :translation/lang   :ru-wrong}
                                   {:translation/entity :root}])
                       (agg/validate validators))]
    (t/is (= [(d/datom 5 :error/attribute :translation/lang)
              (d/datom 5 :error/entity 4)
              (d/datom 5 :error/type :required)

              (d/datom 6 :error/attribute :translation/lang)
              (d/datom 6 :error/entity 3)
              (d/datom 6 :error/type :predicate)
              (d/datom 6 :error/value :ru-wrong)

              (d/datom 7 :error/entity 1)
              (d/datom 7 :error/type :translation/full)
              (d/datom 7 :translation.full/missed #{:ru})]
             (d/seek-datoms agg :eavt 5)))))
