(ns publicator.domain.aggregates.gallery
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]
   [darkleaf.multidecorators :as md]))

(derive :agg/gallery :agg/publication)

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:gallery/image-urls #".{1,255}"})
      (agg/required-validator  'published
                               #{:gallery/image-urls})))
(md/decorate agg/validate :agg/gallery #'validate-decorator)

(def blank
  (-> publication/blank
      (vary-meta assoc :type :agg/gallery)
      (agg/extend-schema {:gallery/image-urls {:db/cardinality :db.cardinality/many}})))
