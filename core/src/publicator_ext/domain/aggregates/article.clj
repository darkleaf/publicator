(ns publicator-ext.domain.aggregates.article
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.aggregates.publication :as publication]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]))

(defmethod aggregate/schema :article [_] publication/+schema+)

(defmethod aggregate/validator :article [chain]
  (-> chain
      (publication/validator)
      (validation/types [:article/image-url string?]
                        [:article.translation/content string?])
      (validation/required-for publication/published-q
                               [:article/image-url not-empty])
      (validation/required-for publication/published-translations-q
                               [:article.translation/content not-empty])))

(defn build [tx-data]
  (let [id (id-generator/generate :publication)]
    (aggregate/build :article id tx-data)))
