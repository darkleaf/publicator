(ns publicator.domain.aggregates.publication
  (:require
   [publicator.util.coll :as u.coll]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.languages :as langs]))

(def states #{:active :archived})
(def translation-states #{:draft :published})

(defn- rules-d [super agg]
  (conj (super agg)
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
          [?e :publication.related/publication :root]]))

(defn- validate-d [super agg]
  (-> (super agg)

      (agg/required-validator  'root
                               #{:publication/state})
      (agg/predicate-validator 'root
                               {:publication/state     states
                                :publication/stream-id pos-int?})
      (agg/required-validator  'published
                               #{:publication/stream-id})
      (agg/query-validator     'root
                               '[:find [?lang ...]
                                 :with ?trans
                                 :where
                                 [?trans :publication.translation/publication ?e]
                                 [?trans :publication.translation/lang ?lang]]
                               u.coll/distinct?)

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
                                :publication.translation/published-at inst?})

      (agg/required-validator  'published-translation
                               #{:publication.translation/title
                                 :publication.translation/summary
                                 :publication.translation/published-at})

      (agg/required-validator  'related
                               #{:publication.related/id
                                 :publication.related/type})
      (agg/predicate-validator 'related
                               {:publication.related/id   pos-int?
                                :publication.related/type keyword?})))

(def blank
  (-> agg/blank
      (agg/extend-schema {:publication.related/publication     {:db/valueType :db.type/ref}
                          :publication.translation/publication {:db/valueType :db.type/ref}
                          :publication.translation/tags        {:db/cardinality :db.cardinality/many}})
      (agg/decorate {`agg/rules    #'rules-d
                     `agg/validate #'validate-d})))
