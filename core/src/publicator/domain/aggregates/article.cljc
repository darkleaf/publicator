(ns publicator.domain.aggregates.article
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.publication :as publication]))

(defn- validate-d [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:article/image-url #".{1,255}"})
      (agg/required-validator  'published
                               #{:article/image-url})

      (agg/predicate-validator 'translation
                               {:article.translation/content #".{1,}"})
      (agg/required-validator  'published-translation
                               #{:article.translation/content})))

(def blank
  (-> publication/blank
      (vary-meta assoc :type :agg/article)
      (agg/decorate {`agg/validate #'validate-d})))
