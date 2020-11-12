(ns publicator.core.domain.aggregates.publication
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]))

(def states #{:active :archived})
(def translation-states #{:draft :published})
(def types #{:article :gallery})

(swap! agg/schema merge
       {:publication/state                    {:db/index true :agg/predicate states}
        :publication/type                     {:agg/predicate types}
        :publication/author-id                {:agg/predicate pos-int?}
        :publication/related-id               {:db/cardinality :db.cardinality/many
                                               :agg/predicate  pos-int?}
        :publication.translation/publication  {:db/valueType :db.type/ref}
        :publication.translation/lang         {:db/index      true
                                               :agg/predicate langs/languages
                                               :agg/uniq      true}
        :publication.translation/state        {:db/index      true
                                               :agg/predicate translation-states}
        :publication.translation/title        {:agg/predicate #".{1,255}"}
        :publication.translation/summary      {:agg/predicate #".{1,255}"}
        :publication.translation/published-at {:agg/predicate inst?}
        :publication.translation/tag          {:db/cardinality :db.cardinality/many
                                               :agg/predicate  #".{1,255}"}
        :article/image-url                    {:agg/predicate #".{1,255}"}
        :article.translation/content          {:agg/predicate #".{1,}"}
        :gallery/image-url                    {:db/cardinality :db.cardinality/many
                                               :agg/predicate  #".{1,255}"}})

(defn article? [agg]
  (agg/include? agg :root :publication/type :article))

(defn gallery? [agg]
  (agg/include? agg :root :publication/type :gallery))

(defn validate [agg]
  (cond-> agg
    :always
    (agg/validate)
    :always
    (agg/required-validator
     {:root                                       [:publication/state
                                                   :publication/type
                                                   :publication/author-id]
      :publication.translation/_publication       [:publication.translation/title
                                                   :publication.translation/state
                                                   :publication.translation/lang]
      [:publication.translation/state :published] [:publication.translation/published-at
                                                   :publication.translation/summary]})
    (article? agg)
    (agg/required-validator
     {:root                                       [:article/image-url]
      [:publication.translation/state :published] [:article.translation/content]})
    (gallery? agg)
    (agg/required-validator {:root [:gallery/image-url]})))
