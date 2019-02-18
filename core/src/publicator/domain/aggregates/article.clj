(ns publicator.domain.aggregates.article
  (:require
   [publicator.domain.aggregate :as aggregate]
   [publicator.domain.aggregates.publication :as publication]
   [publicator.domain.utils.validation :as validation]
   [publicator.domain.abstractions.id-generator :as id-generator]))

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
  (let [id (id-generator/generate :article)]
    (aggregate/build :article id tx-data)))
