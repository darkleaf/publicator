(ns publicator.core.domain.aggregates.gallery
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(derive :agg/gallery :agg/publication)

(md/decorate agg/validate :agg/gallery
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator
         {:gallery/image-urls #".{1,255}"})
        (agg/required-validator
         {:root [:gallery/image-urls]}))))

(md/decorate agg/schema :agg/gallery
  (fn [super type]
    (assoc (super type)
           :gallery/image-urls {:db/cardinality :db.cardinality/many})))
