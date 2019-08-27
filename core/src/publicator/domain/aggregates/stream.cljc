(ns publicator.domain.aggregates.stream
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.languages :as langs]
   [publicator.util :as u]
   [clojure.core.match :as m]))

(def states #{:active :archived})

(defn- rules-d [super agg]
  (conj (super agg)
        '[(translation ?e)
          [?e :stream.translation/stream :root]]))

(defn- validate-d [super agg]
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
                               #(u/same-items? % langs/languages))

      (agg/predicate-validator 'translation
                               {:stream.translation/lang langs/languages
                                :stream.translation/name #".{1,255}"})
      (agg/required-validator  'translation
                               #{:stream.translation/lang
                                 :stream.translation/name})))

(defn- msg->tx-d [super agg msg]
  (m/match msg
    [:stream/add-translation tmp-id]
    [[:db/add tmp-id :stream.translation/stream :root]]

    :else (super agg msg)))

(def blank
  (-> agg/blank
      (vary-meta assoc :type :agg/stream)
      (agg/extend-schema {:stream.translation/stream {:db/valueType :db.type/ref}})
      (agg/decorate {`agg/rules    #'rules-d
                     `agg/validate #'validate-d
                     `agg/msg->tx  #'msg->tx-d})))
