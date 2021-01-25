(ns publicator.core.domain.aggregates.translation
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]))

(def langs [:en :ru])
(def default-lang (first langs))

(swap! agg/schema-of-aggregate merge
       {:translation/entity      {:db/valueType :db.type/ref}
        :translation/entity+lang {:db/tupleAttrs [:translation/entity :translation/lang]
                                  :db/unique     :db.unique/identity}
        :translation.full/missed {:db/cardinality :db.cardinality/many}})

(swap! agg/schema-of-validators merge
       {:translation.full/ident {:db/unique :db.unique/identity}})

(defn upsert-transaction-full-validator [agg entity-rule]
  ;; правило - это вектор, содержащий список, из-за этого они несравнимы
  ;; и его нельзя использовать в качестве значения для идексируемого атрибута
  ;; hash может порождать коллизии, но datascript все равно его использует
  (->> [{:translation.full/ident (hash entity-rule)
         :translation.full/rule  entity-rule
         :validator/type         :translation/full
         :validator/attribute    :translation/lang}]
       (d/db-with agg)))

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

(defn upsert-validators [validators]
  (-> validators
      (agg/upsert-predicate-validator :translation/lang langs)
      (agg/upsert-required-validator  :translation/lang entity-rule)))
