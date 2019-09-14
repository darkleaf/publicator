(ns publicator.domain.aggregates.gallery
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(derive :agg/gallery :agg/publication)

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:gallery/image-urls #".{1,255}"})
      (agg/required-validator  'published
                               #{:gallery/image-urls})))
(md/decorate agg/validate :agg/gallery #'validate-decorator)

(defn schema-decorator [super tag]
  (assoc (super tag)
         :gallery/image-urls {:db/cardinality :db.cardinality/many}))
(md/decorate agg/schema :agg/gallery #'schema-decorator)
