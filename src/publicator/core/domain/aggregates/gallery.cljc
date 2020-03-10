(ns publicator.core.domain.aggregates.gallery
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.publication :as publication]))

(swap! agg/schema merge
       {:gallery/image-urls {:db/cardinality :db.cardinality/many
                             :agg/predicate  #".{1,255}"}})

(defn validate [agg]
  (-> agg
      (publication/validate)
      (agg/required-validator {:root [:gallery/image-urls]})))
