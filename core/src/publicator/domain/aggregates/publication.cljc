(ns publicator.domain.aggregates.publication
  (:require
   [publicator.util :as u]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.languages :as langs]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def translation-states #{:draft :published})

(md/decorate agg/rules :agg/publication
  (fn [super type]
    (conj (super type)
          '[(published ?e)
            [?e :db/ident :root]
            [?translation :publication.translation/publication ?e]
            [?translation :publication.translation/state :published]]
          '[(translation ?e)
            [?e :publication.translation/publication :root]]
          '[(published-translation ?e)
            [?e :publication.translation/publication :root]
            [?e :publication.translation/state :published]]
          '[(related ?e)
            [?e :publication.related/publication :root]])))

(md/decorate agg/validate :agg/publication
  (fn [super agg]
    (-> (super agg)

        (agg/required-validator  'root
                                 #{:publication/state})
        (agg/predicate-validator 'root
                                 {:publication/state     states
                                  :publication/stream-id #'pos-int?})
        (agg/required-validator  'published
                                 #{:publication/stream-id})
        (agg/query-validator     'root
                                 '[:find [?lang ...]
                                   :with ?trans
                                   :where
                                   [?trans :publication.translation/publication ?e]
                                   [?trans :publication.translation/lang ?lang]]
                                 u/distinct-coll?)

        (agg/required-validator  'translation
                                 #{:publication.translation/title
                                   :publication.translation/state
                                   :publication.translation/lang})
        (agg/predicate-validator 'translation
                                 {:publication.translation/lang         langs/languages
                                  :publication.translation/state        translation-states
                                  :publication.translation/title        #".{1,255}"
                                  :publication.translation/summary      #".{1,255}"
                                  :publication.translation/tags         #".{1,255}"
                                  :publication.translation/published-at #'inst?})

        (agg/required-validator  'published-translation
                                 #{:publication.translation/title
                                   :publication.translation/summary
                                   :publication.translation/published-at})

        (agg/required-validator  'related
                                 #{:publication.related/id
                                   :publication.related/type})
        (agg/predicate-validator 'related
                                 {:publication.related/id   #'pos-int?
                                  :publication.related/type #'keyword?}))))

(md/decorate agg/schema :agg/publication
  (fn [super type]
    (assoc (super type)
           :publication.related/publication     {:db/valueType :db.type/ref}
           :publication.translation/publication {:db/valueType :db.type/ref}
           :publication.translation/tags        {:db/cardinality :db.cardinality/many})))
