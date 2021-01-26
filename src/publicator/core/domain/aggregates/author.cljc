(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]))

(swap! agg/schema-of-aggregate merge
       {:author.achivement/root {:db/valueType :db.type/ref}})

(def translation-entity-rule
  '[[(entity ?e)
     [?e :translation/entity :root]]])

(def achivement-entity-rule
  '[[(entity ?e)
     [?e :author.achivement/root :root]]])

;; автор - это примесь к юзеру
(defn upsert-validators [user-validators]
  (-> user-validators
      (translation/upsert-validators)
      (translation/upsert-transaction-full-validator agg/root-entity-rule)
      (agg/upsert-predicate-validator :author.translation/first-name #".{1,255}")
      (agg/upsert-required-validator  :author.translation/first-name translation-entity-rule)

      (agg/upsert-predicate-validator :author.translation/last-name #".{1,255}")
      (agg/upsert-required-validator  :author.translation/last-name translation-entity-rule)

      (agg/upsert-predicate-validator :author.achivement/kind [:legend :star :old-timer])
      (agg/upsert-required-validator  :author.achivement/kind achivement-entity-rule)

      (agg/upsert-predicate-validator :author.achivement/assigner-id int?)
      (agg/upsert-required-validator  :author.achivement/assigner-id achivement-entity-rule)))

;; TODO: под ачивки нужно сделать юзкейс
;; в форму добавлять ключи вроде :author.achivement/can-remove?
;; ну и валидировать, что текущий пользователь может удалять ачивку
