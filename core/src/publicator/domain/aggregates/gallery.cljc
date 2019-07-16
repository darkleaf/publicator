(ns publicator.domain.aggregates.gallery
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]))

(defn- validate-d [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:gallery/image-urls #".{1,255}"})
      (agg/required-validator  'published
                               #{:gallery/image-urls})))

(def blank
  (-> publication/blank
      (vary-meta assoc :type :agg/galery)
      (agg/extend-schema {:gallery/image-urls {:db/cardinality :db.cardinality/many}})
      (agg/decorate {`agg/validate #'validate-d})))
