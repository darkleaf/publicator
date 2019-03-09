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
   :defaults-tx (fn [] [[:db/add :root :root/id (id-generator/*generate* :stream)]
                        [:db/add :root :stream/state :active]])
   :validator
   (d.validation/compose
    (d.validation/attributes [:stream/state states]
                             [:stream.translation/lang langs/languages]
                             [:stream.translation/name string?])
    (d.validation/in-case-of agg/root-q
                             [:stream/state some?])
    (d.validation/in-case-of translations-q
                             [:stream.translation/lang some?]
                             [:stream.translation/name not-empty])
    (d.validation/query-resp agg/root-q
                             '{:find  [[?lang ...]]
                               :in    [$ ?e]
                               :with  [?trans]
                               :where [[?trans :stream.translation/stream ?e]
                                       [?trans :stream.translation/lang ?lang]]}
                             u.c/match? langs/languages))})
