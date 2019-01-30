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
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:opt :gallery/image-urls string?]])
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]
                                [?e :publication/state :active]
                                [?translation :publication.translation/publication ?e]
                                [?translation :publication.translation/state :published]]]
                             [[:req :gallery/image-urls not-empty]])))

(defn build [tx-data]
  (let [id (id-generator/generate :publication)]
    (aggregate/build :gallery id tx-data)))
