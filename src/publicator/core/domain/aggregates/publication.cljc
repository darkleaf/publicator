(ns publicator.core.domain.aggregates.publication
  (:require
   [publicator.util :as u]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]))

(def states #{:active :archived})
(def translation-states #{:draft :published})

(swap! agg/schema assoc
       :publication/state                    {:db/index true :agg/predicate states}
       :publication/stream-id                {:agg/predicate pos-int?}

       :publication.translation/publication  {:db/valueType :db.type/ref}
       :publication.translation/lang         {:agg/predicate langs/languages}
       :publication.translation/state        {:agg/predicate translation-states}
       :publication.translation/title        {:agg/predicate #".{1,255}"}
       :publication.translation/summary      {:agg/predicate #".{1,255}"}
       :publication.translation/published-at {:agg/predicate inst?}
       :publication.translation/tags         {:db/cardinality :db.cardinality/many
                                              :agg/predicate  #".{1,255}"}

       :publication.related/publication      {:db/valueType :db.type/ref}
       :publication.related/id               {:agg/predicate pos-int?}
       :publication.related/type             {:agg/predicate keyword?})

(defn validate [agg]
  (-> agg
      (agg/validate)
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
      (agg/uniq-validator :publication.translation/lang)))
