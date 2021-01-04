(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]))

(def schema
  (merge translation/schema
         '{:author.translation/first-name {:agg/predicate #".{1,255}"}
           :author.translation/last-name  {:agg/predicate #".{1,255}"}
           :author.achivement/root        {:db/valueType :db.type/ref}
           :author.achivement/kind        {:agg/predicate [:legend :star :old-timer]}
           :author.achivement/assigner-id {:agg/predicate int?}}))

; TODO: под ачивки нужно сделать юзкейс
; в форму добавлять ключи вроде :author.achivement/can-remove?
; ну и валидировать, что текущий пользователь может удалять ачивку

(def build (agg/->build schema))

(defn validate [agg]
  (-> agg
      (agg/abstract-validate)
      (translation/validate :full-translation true)
      (agg/required-attrs-validator
       {:translation/_root       #{:author.translation/first-name
                                   :author.translation/last-name}
        :author.achivement/_root #{:author.achivement/kind
                                   :author.achivement/assigner-id}})))
