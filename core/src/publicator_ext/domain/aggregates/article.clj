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
      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :db/ident :root]
                                         [?translation :publication.translation/publication ?e]
                                         [?translation :publication.translation/state :published]]}
                               [:article/image-url not-empty])
      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :publication.translation/publication :root]
                                         [?e :publication.translation/state :published]]}
                               [:article.translation/content not-empty])))

(defn build [tx-data]
  (let [id (id-generator/generate :publication)]
    (aggregate/build :article id tx-data)))
