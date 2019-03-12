(ns publicator.domain.aggregates.author
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.domain.languages :as langs]
   [publicator.utils.coll :as u.c]))

(def states #{:active :archived})
(def stream-participation-roles #{:regular :admin})

(def translations-q
  '{:find  [[?e ...]]
    :where [[?e :author.translation/author :root]]})

(def stream-participations-q
  '{:find  [[?e ...]]
    :where [[?e :author.stream-participation/author :root]]})

(def spec
  {:type   :author
   :schema {:author.translation/author          {:db/valueType :db.type/ref}
            :author.stream-participation/author {:db/valueType :db.type/ref}}
   :validator
   (d.validation/compose
    (d.validation/predicate [[:author/state states]
                             [:author.translation/lang langs/languages]
                             [:author.translation/first-name string?]
                             [:author.translation/first-name not-empty]
                             [:author.translation/last-name string?]
                             [:author.translation/last-name not-empty]
                             [:author.stream-participation/role stream-participation-roles]
                             [:author.stream-participation/stream-id pos-int?]])

    (d.validation/required agg/root-q
                           #{:author/state})

    (d.validation/required translations-q
                           #{:author.translation/lang
                             :author.translation/first-name
                             :author.translation/last-name})

    (d.validation/required stream-participations-q
                           #{:author.stream-participation/role
                             :author.stream-participation/stream-id})

    (d.validation/query agg/root-q
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :author.translation/author ?e]
                                  [?trans :author.translation/lang ?lang]]}
                        u.c/match? langs/languages)

    (d.validation/query agg/root-q
                        '{:find  [[?stream-id ...]]
                          :in    [$ ?e]
                          :with  [?part]
                          :where [[?part :author.stream-participation/author ?e]
                                  [?part :author.stream-participation/stream-id ?stream-id]]}
                        u.c/distinct?))})
