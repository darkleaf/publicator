(ns publicator.core.domain.aggregates.publication
  (:require
   [publicator.util :as u]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def translation-states #{:draft :published})

(md/decorate agg/schema :agg/publication
  (fn [super type]
    (assoc (super type)
           :publication.related/publication     {:db/valueType :db.type/ref}
           :publication.translation/publication {:db/valueType :db.type/ref}
           :publication.translation/state       {:db/index true}
           :publication.translation/tags        {:db/cardinality :db.cardinality/many})))

(md/decorate agg/validate :agg/publication
  (fn [super agg]
    (-> (super agg)
        (agg/required-validator
         {:root                                       [:publication/state
                                                       :publication/stream-id]
          :publication.translation/_publication       [:publication.translation/title
                                                       :publication.translation/state
                                                       :publication.translation/lang]
          [:publication.translation/state :published] [:publication.translation/published-at
                                                       :publication.translation/summary]
          :publication.related/_publication           [:publication.related/id
                                                       :publication.related/type]})
        (agg/predicate-validator
         {:publication/state                    states
          :publication/stream-id                #'pos-int?
          :publication.translation/lang         langs/languages
          :publication.translation/state        translation-states
          :publication.translation/title        #".{1,255}"
          :publication.translation/summary      #".{1,255}"
          :publication.translation/tags         #".{1,255}"
          :publication.translation/published-at #'inst?
          :publication.related/id               #'pos-int?
          :publication.related/type             #'keyword?})

        #_(agg/query-validator 'root
                               '[:find [?lang ...]
                                 :with ?trans
                                 :where
                                 [?trans :publication.translation/publication ?e]
                                 [?trans :publication.translation/lang ?lang]]
                               u/distinct-coll?))))
