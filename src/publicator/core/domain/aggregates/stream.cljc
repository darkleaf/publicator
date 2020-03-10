(ns publicator.core.domain.aggregates.stream
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]))

(def states #{:active :archived})

(swap! agg/schema assoc
       :stream/state              {:agg/predicate states}
       :stream.translation/stream {:db/valueType :db.type/ref}
       :stream.translation/lang   {:agg/predicate langs/languages}
       :stream.translation/name   {:agg/predicate #".{1,255}"})

(defn validate [agg]
  (-> agg
      (agg/validate)
      (agg/required-validator
       {:root                       [:stream/state]
        :stream.translation/_stream [:stream.translation/lang
                                     :stream.translation/name]})
      (agg/uniq-validator :stream.translation/lang)
      (agg/count-validator :stream.translation/lang (count langs/languages))))
