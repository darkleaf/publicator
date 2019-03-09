(ns publicator.domain.aggregates.gallery
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.aggregates.publication :as publication]
   [publicator.utils.datascript.validation :as d.validation]))

(def spec
  (agg/extend-spec
   publication/spec
   {:type        :gallery
    :schema      {:gallery/image-urls {:db/cardinality :db.cardinality/many}}
    :defaults-tx (fn [] [[:db/add :root :root/id (id-generator/*generate* :gallery)]])
    :validator   (d.validation/compose
                  (d.validation/attributes [:gallery/image-urls string?])
                  (d.validation/in-case-of publication/published-q
                                           [:gallery/image-urls not-empty]))}))
