(ns publicator.domain.aggregates.publication
  (:require
   [publicator.domain.aggregate :as aggregate]
   [publicator.domain.util.validation :as validation]
   [publicator.domain.languages :as langs]
   [publicator.util :as u]))

(def ^:const +states+ #{:active :archived})
(def ^:const +translation-states+ #{:draft :published})

(def ^:const published-q
  '{:find  [[?e ...]]
    :where [[?e :db/ident :root]
            [?translation :publication.translation/publication ?e]
            [?translation :publication.translation/state :published]]})

(def ^:const translations-q
  '{:find  [[?e ...]]
    :where [[?e :publication.translation/publication :root]]})

(def ^:const published-translations-q
  '{:find  [[?e ...]]
    :where [[?e :publication.translation/publication :root]
            [?e :publication.translation/state :published]]})

(defn validator [chain]
  (-> chain
      (validation/types [:publication/state +states+]
                        [:publication/related-ids pos-int?]
                        [:publication/stream-id pos-int?]
                        [:publication.translation/lang langs/+languages+]
                        [:publication.translation/state +translation-states+]
                        [:publication.translation/title string?]
                        [:publication.translation/summary string?]
                        [:publication.translation/tags string?]
                        [:publication.translation/published-at inst?])

      (validation/required-for aggregate/root-q
                               [:publication/state some?])
      (validation/required-for translations-q
                               [:publication.translation/state some?]
                               [:publication.translation/lang  some?])
      (validation/required-for published-translations-q
                               [:publication.translation/title not-empty]
                               [:publication.translation/summary not-empty]
                               [:publication.translation/published-at some?])

      (validation/query aggregate/root-q
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :publication.translation/publication ?e]
                                  [?trans :publication.translation/lang ?lang]]}
                        u/distinct?)))

(def ^:const +schema+ {:publication/related-ids             {:db/cardinality :db.cardinality/many}
                       :publication.translation/publication {:db/valueType :db.type/ref}
                       :publication.translation/tags        {:db/cardinality :db.cardinality/many}})
