(ns publicator.core.domain.aggregates.author
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def stream-participation-roles #{:regular :admin})

(md/decorate agg/schema :agg/author
  (fn [super type]
    (assoc (super type)
           :author.translation/author          {:db/valueType :db.type/ref}
           :author.stream-participation/author {:db/valueType :db.type/ref})))

(md/decorate agg/validate :agg/author
  (fn [super agg]
    (-> (super agg)
        (agg/required-validator
         {:root                                [:author/state]
          :author.translation/_author          [:author.translation/lang
                                                :author.translation/first-name
                                                :author.translation/last-name]
          :author.stream-participation/_author [:author.stream-participation/role
                                                :author.stream-participation/stream-id]})
        (agg/predicate-validator
         {:author/state                          states
          :author.translation/lang               langs/languages
          :author.translation/first-name         #".{1,255}"
          :author.translation/last-name          #".{1,255}"
          :author.stream-participation/role      stream-participation-roles
          :author.stream-participation/stream-id #'pos-int?})

        #_(agg/query-validator 'root
                               '[:find [?lang ...]
                                 :with ?trans
                                 :where
                                 [?trans :author.translation/author ?e]
                                 [?trans :author.translation/lang ?lang]]
                               #'langs/all-languages?)
        (agg/uniq-validator :author.stream-participation/stream-id))))
