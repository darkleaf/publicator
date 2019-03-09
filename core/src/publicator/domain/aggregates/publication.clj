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

   :defaults-tx (fn []
                  [[:db/add :root :publication/state :active]
                   (comment "можно попробовать сразу все переводы создать")])

   :validator (d.validation/compose
               (d.validation/attributes [:publication/state states]
                                        [:publication/stream-id pos-int?]
                                        [:publication.translation/lang langs/languages]
                                        [:publication.translation/state translation-states]
                                        [:publication.translation/title string?]
                                        [:publication.translation/summary string?]
                                        [:publication.translation/tags string?]
                                        [:publication.translation/published-at inst?]
                                        [:publication.related/id pos-int?]
                                        [:publication.related/type keyword?])
               (d.validation/in-case-of agg/root-q
                                        [:publication/state some?])
               (d.validation/in-case-of translations-q
                                        [:publication.translation/state some?]
                                        [:publication.translation/lang  some?])
               (d.validation/in-case-of published-translations-q
                                        [:publication.translation/title not-empty]
                                        [:publication.translation/summary not-empty]
                                        [:publication.translation/published-at some?])
               (d.validation/query-resp agg/root-q
                                        '{:find  [[?lang ...]]
                                          :in    [$ ?e]
                                          :with  [?trans]
                                          :where [[?trans :publication.translation/publication ?e]
                                                  [?trans :publication.translation/lang ?lang]]}
                                        u.c/distinct?))})
