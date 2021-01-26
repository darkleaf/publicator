(ns publicator.core.domain.aggregates.publication
  (:require
   [cljc.java-time.extn.predicates :as time.predicates]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]))

(swap! agg/schema-of-aggregate merge
       {:publication/state             {:db/index true}
        :publication/related-id        {:db/cardinality :db.cardinality/many}
        :publication.translation/state {:db/index true}
        :publication.translation/tag   {:db/cardinality :db.cardinality/many}
        :gallery/image-url             {:db/cardinality :db.cardinality/many}})

(def translation-entity-rule
  '[[(entity ?e)
     [?e :translation/entity :root]]])

(def published-translation-entity-rule
  '[[(entity ?e)
     [?e :translation/entity :root]
     [?e :publication.translation/state :published]]])

(defn- new-publication-validators []
  (-> (agg/new-validators)
      (translation/upsert-validators)
      (agg/upsert-predicate-validator :publication/state [:active :archived])
      (agg/upsert-required-validator  :publication/state agg/root-entity-rule)

      (agg/upsert-predicate-validator :publication/author-id int?)
      (agg/upsert-required-validator  :publication/author-id agg/root-entity-rule)

      (agg/upsert-predicate-validator :publication/related-id int?)

      (agg/upsert-predicate-validator :publication.translation/state [:draft :published])
      (agg/upsert-required-validator  :publication.translation/state translation-entity-rule)

      (agg/upsert-predicate-validator :publication.translation/title #".{1,255}")
      (agg/upsert-required-validator  :publication.translation/title translation-entity-rule)

      (agg/upsert-predicate-validator :publication.translation/summary #".{1,255}")
      (agg/upsert-required-validator  :publication.translation/summary
                                      published-translation-entity-rule)

      (agg/upsert-predicate-validator :publication.translation/published-at
                                      time.predicates/instant?)
      (agg/upsert-required-validator  :publication.translation/published-at
                                      published-translation-entity-rule)

      (agg/upsert-predicate-validator :publication.translation/tag #".{1,255}")))

(defn new-article-validators []
  (-> (new-publication-validators)
      (agg/upsert-predicate-validator :article/image-url #".{1,255}")
      (agg/upsert-required-validator  :article/image-url agg/root-entity-rule)

      (agg/upsert-predicate-validator :article.translation/content #".{1,}")
      (agg/upsert-required-validator  :article.translation/content
                                      published-translation-entity-rule)))

(defn new-gallery-validators []
  (-> (new-publication-validators)
      (agg/upsert-predicate-validator :gallery/image-url #".{1,255}")
      (agg/upsert-required-validator  :gallery/image-url agg/root-entity-rule)))
