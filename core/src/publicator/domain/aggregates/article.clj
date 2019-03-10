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
                  (d.validation/predicate [[:article/image-url string?]
                                           [:article.translation/content string?]])

                  (d.validation/required publication/published-q
                                         #{:article/image-url})

                  (d.validation/predicate publication/published-q
                                          [[:article/image-url not-empty]])

                  (d.validation/required publication/published-translations-q
                                         #{:article.translation/content})

                  (d.validation/predicate publication/published-translations-q
                                          [[:article.translation/content not-empty]]))}))
