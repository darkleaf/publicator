(ns publicator.core.domain.aggregates.stream
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.languages :as langs]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})

(md/decorate agg/schema :agg.stream/base
  (fn [super type]
    (assoc (super type)
           :stream.translation/stream {:db/valueType :db.type/ref}
           :stream.translation/lang {:db/unique :db.unique/identity})))

(md/decorate agg/rules :agg.stream/base
  (fn [super type]
    (conj (super type)
          '[(translation ?e)
            [?e :stream.translation/stream :root]])))

(md/decorate agg/validate :agg.stream/base
  (fn [super agg]
    (-> (super agg)
        (agg/query-validator 'root
          '[:find [?lang ...]
            :with ?trans
            :where
            [?trans :stream.translation/stream ?e]
            [?trans :stream.translation/lang ?lang]]
          #'langs/all-languages?)

        (agg/predicate-validator 'translation
          {:stream.translation/lang langs/languages
           :stream.translation/name #".{1,255}"})
        (agg/required-validator 'translation
          #{:stream.translation/lang
            :stream.translation/name}))))

(md/decorate agg/allowed-attribute? :agg.stream/base
  (fn [super type attr]
    (or (super type attr)
        (#{:stream.translation/name
           :stream.translation/lang
           :stream.translation/stream} attr))))

(md/decorate agg/validate :agg/stream
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:stream/state states})
        (agg/required-validator 'root
          #{:stream/state}))))

(md/decorate agg/allowed-attribute? :agg/stream
  (fn [super type attr]
    (or (super type attr)
        (#{:stream/state} attr))))

(derive :agg/stream :agg/persisting)
(derive :agg/stream :agg.stream/base)
