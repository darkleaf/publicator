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
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:opt :article/image-url string?]])
      (validation/attributes '[[(entity ?e)
                                [?e :publication.translation/publication :root]]]
                             [[:opt :article.translation/content string?]])
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]
                                [?e :publication/state :active]
                                [?translation :publication.translation/publication ?e]
                                [?translation :publication.translation/state :published]]]
                             [[:req :article/image-url not-empty]])
      (validation/attributes '[[(entity ?e)
                                [?e :publication.translation/publication :root]
                                [?e :publication.translation/state :published]
                                [:root :publication/state :active]]]
                             [[:req :article.translation/content not-empty]])))

(defn build [tx-data]
  (let [id (id-generator/generate :publication)]
    (aggregate/build :article id tx-data)))
