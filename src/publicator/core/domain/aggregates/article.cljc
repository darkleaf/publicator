(ns publicator.core.domain.aggregates.article
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(derive :agg/article :agg/publication)

(md/decorate agg/validate :agg/article
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator
         {:article/image-url           #".{1,255}"
          :article.translation/content #".{1,}"})
        (agg/required-validator
         {:root                                 [:article/image-url]
          :publication.translation/_publication [:article.translation/content]}))))
