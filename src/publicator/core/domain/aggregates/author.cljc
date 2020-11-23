(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]))

(def achivement-types #{:legend :star :old-timer})

(swap! agg/schema merge
       {:author.translation/first-name {:agg/predicate #".{1,255}"}
        :author.translation/last-name  {:agg/predicate #".{1,255}"}
        :author.achivement/author      {:db/valueType :db.type/ref}
        :author.achivement/kind        {:agg/predicate achivement-types}
        :author.achivement/assigner-id {:agg/predicate pos-int?}})

(defn validate [agg]
  (-> agg
      (agg/validate)
      (translation/validate)
      (translation/full-translation-validator)
      (agg/required-attrs-validator
       {:translation/_root         [:author.translation/first-name
                                    :author.translation/last-name]
        :author.achivement/_author [:author.achivement/kind
                                    :author.achivement/assigner-id]})))
