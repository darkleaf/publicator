(ns publicator-ext.domain.aggregates.publication
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.languages :as langs]
   [publicator-ext.util :as u]))

(def ^:const +states+ #{:active :archived})
(def ^:const +translation-states+ #{:draft :published})

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

      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :db/ident :root]]}
                               [:publication/state some?])
      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :publication.translation/publication :root]
                                         [?e :publication.translation/state :published]]}
                               [:publication.translation/title not-empty]
                               [:publication.translation/summary not-empty]
                               [:publication.translation/lang some?]
                               [:publication.translation/published-at some?])

      (validation/query '{:find  [[?e ...]]
                          :where [[?e :db/ident :root]]}
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :publication.translation/publication ?e]
                                  [?trans :publication.translation/lang ?lang]]}
                        u/distinct?)))

(def ^:const +schema+ {:publication/related-ids             {:db/cardinality :db.cardinality/many}
                       :publication.translation/publication {:db/valueType :db.type/ref}
                       :publication.translation/tags        {:db/cardinality :db.cardinality/many}})
