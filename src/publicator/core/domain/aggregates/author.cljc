(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]))

(swap! agg/schema merge
       {:author.translation/author     {:db/valueType :db.type/ref}
        :author.translation/lang       {:db/index      true
                                        :agg/predicate langs/languages
                                        :agg/uniq true}
        :author.translation/first-name {:agg/predicate #".{1,255}"}
        :author.translation/last-name  {:agg/predicate #".{1,255}"}})

(defn validate [agg]
  (-> agg
      (agg/validate)
      (agg/required-validator
       {:author.translation/_author [:author.translation/lang
                                     :author.translation/first-name
                                     :author.translation/last-name]})
      (agg/count-validator :author.translation/lang (count langs/languages))))
