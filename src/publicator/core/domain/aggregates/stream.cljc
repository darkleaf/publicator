(ns publicator.core.domain.aggregates.stream
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})

(md/decorate agg/schema :agg/stream
  (fn [super type]
    (assoc (super type)
           :stream.translation/stream {:db/valueType :db.type/ref})))

(md/decorate agg/validate :agg/stream
  (fn [super agg]
    (-> (super agg)
        (agg/required-validator
         {:root                       [:stream/state]
          :stream.translation/_stream [:stream.translation/lang
                                       :stream.translation/name]})
        (agg/predicate-validator
         {:stream/state            states
          :stream.translation/lang langs/languages
          :stream.translation/name #".{1,255}"})
        (agg/uniq-validator :stream.translation/lang)
        (agg/count-validator :stream.translation/lang (count langs/languages)))))
