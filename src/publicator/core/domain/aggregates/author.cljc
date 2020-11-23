(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]))

(def achivement-types #{:legend :star :old-timer})

(swap! agg/schema merge
       {:author.translation/first-name {:agg/predicate #".{1,255}"}
        :author.translation/last-name  {:agg/predicate #".{1,255}"}
        :author.achivement/root        {:db/valueType :db.type/ref}
        :author.achivement/kind        {:agg/predicate achivement-types}
        :author.achivement/assigner-id {:agg/predicate pos-int?}})

; TODO: под ачивки нужно сделать юзкейс
; в форму добавлять ключи вроде :author.achivement/can-remove?
; ну и валидировать, что текущий пользователь может удалять ачивку

(defn validate [agg]
  (-> agg
      (agg/validate)
      (translation/validate)
      (translation/full-translation-validator)
      (agg/required-attrs-validator
       {:translation/_root       [:author.translation/first-name
                                  :author.translation/last-name]
        :author.achivement/_root [:author.achivement/kind
                                  :author.achivement/assigner-id]})))
