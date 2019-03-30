(ns publicator.domain.aggregates.gallery
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.aggregates.publication :as publication]
   [publicator.utils.datascript.validation :as d.validation]))

(def spec
  (agg/merge-spec
   publication/spec
   {:type         :gallery
    :schema       {:gallery/image-urls {:db/cardinality :db.cardinality/many}}
    :id-generator #(id-generator/*generate* :gallery)
    :validator    (d.validation/compose
                   (d.validation/predicate [[:gallery/image-urls string?]])

                   (d.validation/required publication/published-q
                                          #{:gallery/image-urls}))}))
