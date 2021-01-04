(ns publicator.core.domain.aggregates.publication
  (:require
   [cljc.java-time.extn.predicates :as time.predicates]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.translation :as translation]))

(def schema
  (merge translation/schema
         '{:publication/state                    {:db/index      true
                                                  :agg/predicate [:active :archived]}
           :publication/type                     {:agg/predicate [:article :gallery]}
           :publication/author-id                {:agg/predicate int?}
           :publication/related-id               {:db/cardinality :db.cardinality/many
                                                  :agg/predicate  int?}
           :publication.translation/state        {:db/index      true
                                                  :agg/predicate [:draft :published]}
           :publication.translation/title        {:agg/predicate #".{1,255}"}
           :publication.translation/summary      {:agg/predicate #".{1,255}"}
           :publication.translation/published-at {:agg/predicate time/instant?}
           :publication.translation/tag          {:db/cardinality :db.cardinality/many
                                                  :agg/predicate  #".{1,255}"}
           :article/image-url                    {:agg/predicate #".{1,255}"}
           :article.translation/content          {:agg/predicate #".{1,}"}
           :gallery/image-url                    {:db/cardinality :db.cardinality/many
                                                  :agg/predicate  #".{1,255}"}}))

(def build (agg/->build schema))

(defn article? [agg]
  (agg/include? agg :root :publication/type :article))

(defn gallery? [agg]
  (agg/include? agg :root :publication/type :gallery))

(defn validate [agg]
  (cond-> agg
    true (agg/abstract-validate)
    true (translation/validate)
    true (agg/required-attrs-validator
          {:root                                       #{:publication/state
                                                         :publication/type
                                                         :publication/author-id}
           :translation/_root                          #{:publication.translation/title
                                                         :publication.translation/state}
           [:publication.translation/state :published] #{:publication.translation/published-at
                                                         :publication.translation/summary}})

    (article? agg) (agg/required-attrs-validator
                    {:root                                       [:article/image-url]
                     [:publication.translation/state :published] [:article.translation/content]})
    (gallery? agg) (agg/required-attrs-validator {:root [:gallery/image-url]})))
