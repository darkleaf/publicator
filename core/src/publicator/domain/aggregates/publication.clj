(ns publicator.domain.aggregates.publication
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]
   [publicator.utils.datascript.fns :as d.fns]
   [publicator.domain.languages :as langs]
   [publicator.utils.coll :as u.c]))

(def states #{:active :archived})
(def translation-states #{:draft :published})

(def published-q
  '{:find  [[?e ...]]
    :where [[?e :db/ident :root]
            [?translation :publication.translation/publication ?e]
            [?translation :publication.translation/state :published]]})

(def translations-q
  '{:find  [[?e ...]]
    :where [[?e :publication.translation/publication :root]]})

(def published-translations-q
  '{:find  [[?e ...]]
    :where [[?e :publication.translation/publication :root]
            [?e :publication.translation/state :published]]})

(def spec
  {:schema {:publication.related/publication     {:db/valueType :db.type/ref}
            :publication.translation/publication {:db/valueType :db.type/ref}
            :publication.translation/tags        {:db/cardinality :db.cardinality/many}}
   :validator (d.validation/compose

               (d.validation/predicate [[:publication/state states]
                                        [:publication/stream-id pos-int?]
                                        [:publication.translation/lang langs/languages]
                                        [:publication.translation/state translation-states]
                                        [:publication.translation/title string?]
                                        [:publication.translation/summary string?]
                                        [:publication.translation/tags string?]
                                        [:publication.translation/published-at inst?]
                                        [:publication.related/id pos-int?]
                                        [:publication.related/type keyword?]])

               (d.validation/required agg/root-q
                                      #{:publication/state})

               (d.validation/required translations-q
                                      #{:publication.translation/state
                                        :publication.translation/lang})

               (d.validation/required published-translations-q
                                      #{:publication.translation/title
                                        :publication.translation/summary
                                        :publication.translation/published-at})

               (d.validation/predicate published-translations-q
                                       [[:publication.translation/title not-empty]
                                        [:publication.translation/summary not-empty]])

               (d.validation/query agg/root-q
                                   '{:find  [[?lang ...]]
                                     :in    [$ ?e]
                                     :with  [?trans]
                                     :where [[?trans :publication.translation/publication ?e]
                                             [?trans :publication.translation/lang ?lang]]}
                                   u.c/distinct?))})
