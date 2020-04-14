(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]))

(def states #{:active :archived})
(def stream-participation-roles #{:regular :admin})

(swap! agg/schema merge
       {:author/state                          {:agg/predicate states}
        :author.translation/author             {:db/valueType :db.type/ref}
        :author.translation/lang               {:agg/predicate langs/languages
                                                :agg/uniq      true}
        :author.translation/first-name         {:agg/predicate #".{1,255}"}
        :author.translation/last-name          {:agg/predicate #".{1,255}"}
        :author.stream-participation/author    {:db/valueType :db.type/ref}
        :author.stream-participation/role      {:agg/predicate stream-participation-roles}
        :author.stream-participation/stream-id {:agg/predicate pos-int?
                                                :agg/uniq      true}})

(defn validate [agg]
  (-> agg
      (agg/validate)
      (agg/required-validator
       {:root                                [:author/state]
        :author.translation/_author          [:author.translation/lang
                                              :author.translation/first-name
                                              :author.translation/last-name]
        :author.stream-participation/_author [:author.stream-participation/role
                                              :author.stream-participation/stream-id]})
      (agg/count-validator :author.translation/lang (count langs/languages))))
