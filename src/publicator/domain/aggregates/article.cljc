(ns publicator.domain.aggregates.article
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(derive :agg/article :agg/publication)

(md/decorate agg/validate :agg/article
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:article/image-url #".{1,255}"})
        (agg/required-validator  'published
          #{:article/image-url})

        (agg/predicate-validator 'translation
          {:article.translation/content #".{1,}"})
        (agg/required-validator  'published-translation
          #{:article.translation/content}))))
