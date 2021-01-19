(ns publicator.core.domain.aggregates.translation-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest lang-predicate
  (let [validators (-> agg/proto-validators
                       (translation/validators-mixin))
        agg        (-> agg/proto-agg
                       (translation/agg-mixin)
                       (d/db-with [{:translation/entity :root
                                    :translation/lang   :wrong}])
                       (agg/validate validators))]
    (t/is (= [(d/datom 3 :error/attribute :translation/lang)
              (d/datom 3 :error/entity 2)
              (d/datom 3 :error/type :predicate)
              (d/datom 3 :error/value :wrong)]
             (d/seek-datoms agg :eavt 3)))))

(t/deftest required-lang
  (let [validators (-> agg/proto-validators
                       (translation/validators-mixin))
        agg        (-> agg/proto-agg
                       (translation/agg-mixin)
                       (d/db-with [{:translation/entity :root}])
                       (agg/validate validators))]
    (t/is (= [(d/datom 3 :error/attribute :translation/lang)
              (d/datom 3 :error/entity 2)
              (d/datom 3 :error/type :required)]
             (d/seek-datoms agg :eavt 3)))))

(t/deftest missed-translation
  (let [validators (-> agg/proto-validators
                       (translation/validators-mixin)
                       (d/db-with [[:translation.full/upsert agg/root-entity-rule]]))
        agg        (-> agg/proto-agg
                       (translation/agg-mixin)
                       (d/db-with [{:translation/entity :root
                                    :translation/lang   :en}])
                       (agg/validate validators))]
    (t/is (= [(d/datom 3 :error/entity 1)
              (d/datom 3 :error/type :translation/full)
              (d/datom 3 :translation.full/missed #{:ru})]
             (d/seek-datoms agg :eavt 3)))))

(t/deftest missed-all-translations
  (let [validators (-> agg/proto-validators
                       (translation/validators-mixin)
                       (d/db-with [[:translation.full/upsert agg/root-entity-rule]]))
        agg        (-> agg/proto-agg
                       (translation/agg-mixin)
                       (agg/validate validators))]
    (t/is (= [(d/datom 2 :error/entity 1)
              (d/datom 2 :error/type :translation/full)
              (d/datom 2 :translation.full/missed #{:en :ru})]
             (d/seek-datoms agg :eavt 2)))))
