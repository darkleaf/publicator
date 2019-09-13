(ns publicator.domain.aggregates.stream
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.languages :as langs]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})

(defn rules-decorator [super agg]
  (conj (super agg)
        '[(translation ?e)
          [?e :stream.translation/stream :root]]))
(md/decorate agg/rules :agg/stream #'rules-decorator)

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:stream/state states})
      (agg/required-validator  'root
                               #{:stream/state})
      (agg/query-validator     'root
                               '[:find [?lang ...]
                                 :with ?trans
                                 :where
                                 [?trans :stream.translation/stream ?e]
                                 [?trans :stream.translation/lang ?lang]]
                               #'langs/all-languages?)

      (agg/predicate-validator 'translation
                               {:stream.translation/lang langs/languages
                                :stream.translation/name #".{1,255}"})
      (agg/required-validator  'translation
                               #{:stream.translation/lang
                                 :stream.translation/name})))
(md/decorate agg/validate :agg/stream #'validate-decorator)

(def blank
  (-> agg/blank
      (vary-meta assoc :type :agg/stream)
      (agg/extend-schema {:stream.translation/stream {:db/valueType :db.type/ref}})))
