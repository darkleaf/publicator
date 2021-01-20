(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]))

(def proto-agg
  (-> agg/proto-agg
      (translation/agg-mixin)
      (agg/vary-schema
       merge {:author.achivement/root {:db/valueType :db.type/ref}})))

(def translation-entity-rule
  '[[(entity ?e)
     [?e :translation/entity :root]]])

(def achivement-entity-rule
  '[[(entity ?e)
     [?e :author.achivement/root :root]]])

(def validators
  (-> agg/proto-validators
      (translation/validators-mixin)
      (d/db-with [[:translation.full/upsert agg/root-entity-rule]
                  [:predicate/upsert :author.translation/first-name #".{1,255}"]
                  [:required/upsert  :author.translation/first-name translation-entity-rule]

                  [:predicate/upsert :author.translation/last-name #".{1,255}"]
                  [:required/upsert  :author.translation/last-name translation-entity-rule]

                  [:predicate/upsert :author.achivement/kind [:legend :star :old-timer]]
                  [:required/upsert  :author.achivement/kind achivement-entity-rule]

                  [:predicate/upsert :author.achivement/assigner-id int?]
                  [:required/upsert  :author.achivement/assigner-id achivement-entity-rule]])))

; TODO: под ачивки нужно сделать юзкейс
; в форму добавлять ключи вроде :author.achivement/can-remove?
; ну и валидировать, что текущий пользователь может удалять ачивку
