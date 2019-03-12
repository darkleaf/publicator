(ns publicator.domain.aggregates.stream
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.domain.languages :as langs]
   [publicator.utils.coll :as u.c]))

(def states #{:active :archived})

(def translations-q
  '{:find  [[?e ...]]
    :where [[?e :stream.translation/stream :root]]})

(def spec
  {:type        :stream
   :schema      {:stream.translation/stream {:db/valueType :db.type/ref}}
   :defaults-tx (fn [] [[:db/add :root :root/id (id-generator/*generate* :stream)]])
   :validator
   (d.validation/compose
    (d.validation/predicate [[:stream/state states]
                             [:stream.translation/lang langs/languages]
                             [:stream.translation/name string?]
                             [:stream.translation/name not-empty]])

    (d.validation/required agg/root-q
                           #{:stream/state})

    (d.validation/required translations-q
                           #{:stream.translation/lang
                             :stream.translation/name})

    (d.validation/query agg/root-q
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :stream.translation/stream ?e]
                                  [?trans :stream.translation/lang ?lang]]}
                        u.c/match? langs/languages))})
