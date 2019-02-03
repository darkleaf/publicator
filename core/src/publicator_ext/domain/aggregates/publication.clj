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
      (validation/attributes '{:find  [[?e ...]]
                               :where [[?e :db/ident :root]]}
                             [[:req :publication/state +states+]
                              [:opt :publication/related-ids pos-int?] ;;todo
                              [:opt :publication/stream-id pos-int?]])
      (validation/attributes '{:find  [[?e ...]]
                               :where [[?e :publication.traslation/publication :root]]}
                             [[:req :publication.translation/lang langs/+languages+]
                              [:req :publication.translation/state +translation-states+]
                              [:opt :publication.translation/title string?]
                              [:opt :publication.translation/summary string?]
                              [:opt :publication.translation/tags string?]
                              [:opt :publication.translation/published-at inst?]])
      (validation/query '{:find  [[?e ...]]
                          :where [[?e :db/ident :root]]}
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :publication.translation/publication ?e]
                                  [?trans :publication.translation/lang ?lang]]}
                        u/distinct?)
      (validation/attributes '{:find  [[?e ...]]
                               :where [[?e :publication.translation/publication :root]
                                       [?e :publication.translation/state :published]
                                       [:root :publication/state :active]]}
                             [[:req :publication.translation/title not-empty]
                              [:req :publication.translation/summary not-empty]
                              [:req :publication.translation/published-at some?]])))

(def ^:const +schema+ {:publication/related-ids             {:db/cardinality :db.cardinality/many}
                       :publication.translation/publication {:db/valueType :db.type/ref}
                       :publication.translation/tags        {:db/cardinality :db.cardinality/many}})
