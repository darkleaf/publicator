(ns publicator.domain.aggregates.gallery
  (:require
   [publicator.domain.aggregate :as aggregate]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.aggregates.publication :as publication]
   [publicator.domain.utils.validation :as validation]))

(defmethod aggregate/schema :gallery [_]
  (merge publication/+schema+
         {:gallery/image-urls {:db/cardinality :db.cardinality/many}}))

(defmethod aggregate/validator :gallery [chain]
  (-> chain
      (publication/validator)
      (validation/types [:gallery/image-urls string?])

      (validation/required-for publication/published-q
                               [:gallery/image-urls not-empty])))

(defn build [tx-data]
  (let [id (id-generator/generate :gallery)]
    (aggregate/build :gallery id tx-data)))
