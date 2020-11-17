(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]))

(def achivement-types #{:legend :star :old-timer})

(swap! agg/schema merge
       {:author.translation/author     {:db/valueType :db.type/ref}
        :author.translation/lang       {:db/index      true
                                        :agg/predicate langs/languages
                                        :agg/uniq      true}
        :author.translation/first-name {:agg/predicate #".{1,255}"}
        :author.translation/last-name  {:agg/predicate #".{1,255}"}
        :author.achivement/author      {:db/valueType :db.type/ref}
        :author.achivement/kind        {:agg/predicate achivement-types}
        :author.achivement/assigner-id {:agg/predicate pos-int?}})

(defn validate [agg]
  (-> agg
      (agg/validate)
      (agg/required-validator
       {:author.translation/_author [:author.translation/lang
                                     :author.translation/first-name
                                     :author.translation/last-name]
        :author.achivement/_author  [:author.achivement/kind
                                     :author.achivement/assigner-id]})
      (agg/count-validator :author.translation/lang (count langs/languages))))
