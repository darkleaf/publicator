(ns publicator.core.domain.aggregates.translation
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]))

(def langs [:en :ru])
(def default-lang (first langs))

(defn agg-mixin [agg]
  (-> agg
      (agg/vary-schema merge
                       {:translation/entity {:db/valueType :db.type/ref}
                        :translation/lang   {:db/unique     :db.unique/identity}})))

(defn- transaction-full-upsert-tx [_agg]
  [{:db/ident            :translation/full
    :validator/type      :translation/full
    :validator/attribute :translation/lang}])

(defmethod agg/errors-tx :translation/full [agg _validator]
  (let [missed-langs (d/q '[:find ?e (aggregate ?set ?lang)
                            :in $ ?set [?lang ...]
                            :where
                            [?t :translation/entity ?e]
                            (not-join [?lang]
                              [?t :translation/lang ?lang])]
                          agg set langs)]
    (for [[e langs] missed-langs]
      {:error/type              :translation/full
       :error/entity            e
       :translation.full/missed langs})))

(def entity-rule
  '[[(entity ?e)
     [?e :translation/entity _]]])

(defn validators-mixin [validators]
  (-> validators
      (d/db-with [{:db/ident :translation.full/upsert
                   :db/fn    transaction-full-upsert-tx}
                  [:predicate/upsert :translation/lang langs]
                  [:required/upsert :translation/lang entity-rule]])))
