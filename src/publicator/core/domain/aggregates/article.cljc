(ns publicator.core.domain.aggregates.article
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.publication :as publication]))

(swap! agg/schema merge
       {:article/image-url           {:agg/predicate #".{1,255}"}
        :article.translation/content {:agg/predicate #".{1,}"}})

(defn validate [agg]
  (-> agg
      (publication/validate)
      (agg/required-validator
       {:root                                        [:article/image-url]
        [:publication.translation/state "published"] [:article.translation/content]})))
