(ns publicator.domain.aggregates.article
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]
   [darkleaf.multidecorators :as md]))

(derive :agg/article :agg/publication)

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:article/image-url #".{1,255}"})
      (agg/required-validator  'published
                               #{:article/image-url})

      (agg/predicate-validator 'translation
                               {:article.translation/content #".{1,}"})
      (agg/required-validator  'published-translation
                               #{:article.translation/content})))
(md/decorate agg/validate :agg/article #'validate-decorator)

(def blank
  (-> publication/blank
      (vary-meta assoc :type :agg/article)))
