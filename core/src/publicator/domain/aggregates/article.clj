(ns publicator.domain.aggregates.article
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.domain.abstractions.id-generator :as id-generator]))

(def spec
  (agg/extend-spec
   publication/spec
   {:type        :article
    :defaults-tx (fn [] [[:db/add :root :root/id (id-generator/*generate* :article)]])
    :validator   (d.validation/compose
                  (d.validation/attributes [:article/image-url string?]
                                           [:article.translation/content string?])
                  (d.validation/in-case-of publication/published-q
                                           [:article/image-url not-empty])
                  (d.validation/in-case-of publication/published-translations-q
                                           [:article.translation/content not-empty]))}))
