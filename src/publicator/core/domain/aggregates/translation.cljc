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

(defn- transaction-full-upsert-tx [_agg entity-rule]
  ;; правило - это вектор, содержащий список, из-за этого они несравнимы
  ;; hash может порождать коллизии, но datascript все равно его использует
  [{:translation.full/ident (hash entity-rule)
    :validator/type         :translation/full
    :validator/attribute    :translation/lang
    :translation.full/rule  entity-rule}])

(defmethod agg/errors-tx :translation/full [agg {:keys [translation.full/rule]}]
  (let [missed-langs (d/q '[:find ?e (aggregate ?set ?lang)
                            :in $ % ?set [?lang ...]
                            :where
                            (entity ?e)
                            (not-join [?lang]
                              [?t :translation/entity ?e]
                              [?t :translation/lang ?lang])]
                          agg rule set langs)]
    (for [[e langs] missed-langs]
      {:error/type              :translation/full
       :error/entity            e
       :translation.full/missed langs})))

(def entity-rule
  '[[(entity ?e)
     [?e :translation/entity _]]])

(defn validators-mixin [validators]
  (-> validators
      (agg/vary-schema merge {:translation.full/ident {:db/unique :db.unique/identity}
                              :translation.full/rule  {:db/cardinality :db.cardinality/many}})
      (d/db-with [{:db/ident :translation.full/upsert
                   :db/fn    transaction-full-upsert-tx}
                  [:predicate/upsert :translation/lang langs]
                  [:required/upsert :translation/lang entity-rule]])))
