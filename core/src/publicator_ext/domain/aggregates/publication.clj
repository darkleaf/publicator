(ns publicator-ext.domain.aggregates.publication
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.languages :as langs]))

(def ^:const +states+ #{:active :archived})
(def ^:const +translation-states+ #{:draft :published})


(defn validator [chain]
  (-> chain
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:req :publication/state +states+]
                              [:opt :publication/related-ids pos-int?] ;;todo
                              [:opt :publication/stream-id pos-int?]])
      (validation/attributes '[[(entity ?e)
                                [?e :publication.traslation/publication :root]]]
                             [[:req :publication.translation/lang langs/+languages+]
                              [:req :publication.translation/state +translation-states+]
                              [:opt :publication.translation/title string?]
                              [:opt :publication.translation/summary string?]
                              [:opt :publication.translation/tags string?]
                              [:opt :publication.translation/published-at inst?]])
      (validation/attributes '[[(entity ?e)
                                [?e :publication.translation/publication :root]
                                [?e :publication.translation/state :published]
                                [:root :publication/state :active]]]
                             [[:req :publication.translation/title not-empty]
                              [:req :publication.translation/summary not-empty]
                              [:req :publication.translation/published-at some?]])))

(def ^:const +schema+ {:publication/related-ids             {:db/cardinality :db.cardinality/many}
                       :publication.translation/publication {:db/valueType :db.type/ref}
                       :publication.translation/lang        {:db/unique :db.unique/identity}
                       :publication.translation/tags        {:db/cardinality :db.cardinality/many}})
