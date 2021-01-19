(ns publicator.core.domain.aggregates.publication
  (:require
   [cljc.java-time.extn.predicates :as time.predicates]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]
   [datascript.core :as d]))

(def proto-agg
  (-> agg/proto-agg
      (translation/agg-mixin)
      (agg/vary-schema
       merge {:publication/state             {:db/index true}
              :publication/related-id        {:db/cardinality :db.cardinality/many}
              :publication.translation/state {:db/index true}
              :publication.translation/tag   {:db/cardinality :db.cardinality/many}
              :gallery/image-url             {:db/cardinality :db.cardinality/many}})))

(def translation-entity-rule
  '[[(entity ?e)
     [?e :translation/entity :root]]])

(def published-translation-entity-rule
  '[[(entity ?e)
     [?e :translation/entity :root]
     [?e :publication.translation/state :published]]])

(def publication-validators
  (-> agg/proto-validators
      (translation/validators-mixin)
      (d/db-with [[:predicate/upsert :publication/state [:active :archived]]
                  [:required/upsert  :publication/state agg/root-entity-rule]

                  [:predicate/upsert :publication/author-id int?]
                  [:required/upsert  :publication/author-id agg/root-entity-rule]

                  [:predicate/upsert :publication/related-id int?]

                  [:predicate/upsert :publication.translation/state [:draft :published]]
                  [:required/upsert  :publication.translation/state translation-entity-rule]

                  [:predicate/upsert :publication.translation/title #".{1,255}"]
                  [:required/upsert  :publication.translation/title translation-entity-rule]

                  [:predicate/upsert :publication.translation/summary #".{1,255}"]
                  [:required/upsert  :publication.translation/summary
                   published-translation-entity-rule]

                  [:predicate/upsert :publication.translation/published-at
                   time.predicates/instant?]
                  [:required/upsert  :publication.translation/published-at
                   published-translation-entity-rule]

                  [:predicate/upsert :publication.translation/tag #".{1,255}"]])))

(def article-validators
  (-> publication-validators
      (d/db-with [[:predicate/upsert :article/image-url #".{1,255}"]
                  [:required/upsert  :article/image-url agg/root-entity-rule]

                  [:predicate/upsert :article.translation/content #".{1,}"]
                  [:required/upsert  :article.translation/content
                   published-translation-entity-rule]])))

(def gallery-validators
  (-> publication-validators
      (d/db-with [[:predicate/upsert :gallery/image-url #".{1,255}"]
                  [:required/upsert  :gallery/image-url agg/root-entity-rule]])))
