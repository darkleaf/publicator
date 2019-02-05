(ns publicator-ext.domain.aggregates.gallery
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.aggregates.publication :as publication]
   [publicator-ext.domain.util.validation :as validation]))

(defmethod aggregate/schema :gallery [_]
  (merge publication/+schema+
         {:gallery/image-urls {:db/cardinality :db.cardinality/many}}))

(defmethod aggregate/validator :gallery [chain]
  (-> chain
      (publication/validator)
      (validation/types [:gallery/image-urls string?])

      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :db/ident :root]
                                         [?translation :publication.translation/publication ?e]
                                         [?translation :publication.translation/state :published]]}
                               [:gallery/image-urls not-empty])))

(defn build [tx-data]
  (let [id (id-generator/generate :publication)]
    (aggregate/build :gallery id tx-data)))
