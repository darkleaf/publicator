(ns publicator-ext.domain.aggregates.publication
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.errors :as errors]
   [publicator-ext.domain.languages :as langs]
   [clojure.spec.alpha :as s]))

(def ^:const +states+ #{:active :archived})
(def ^:const +translation-states+ #{:draft :published})

(s/def :publication/state +states+)
(s/def :publication/related-ids (s/coll-of :aggregate/id))
(s/def :publication/stream-id :aggregate/id)

(s/def :publication.translation/lang langs/+languages+)
(s/def :publication.translation/title string?)
(s/def :publication.translation/summary string?)
(s/def :publication.translation/tags (s/coll-of string?))
(s/def :publication.translation/published-at inst?)
(s/def :publication.translation/state +translation-states+)

(s/def ::publication
  (s/merge :aggregate/root
           (s/keys :req [:publication/state]
                   :opt [:publication/related-ids
                         :publication/stream-id])))

(s/def ::translation
  (s/merge :aggregate/entity
           (s/keys :req [:publication.translation/publication
                         :publication.translation/lang
                         :publication.translation/state]
                   :opt [:publication.translation/title
                         :publication.translation/summary
                         :publication.translation/tags
                         :publication.translation/published-at])))

(def ^:const +schema+ {:publication/related-ids             {:db/cardinality :db.cardinality/many}
                       :publication.translation/publication {:db/valueType :db.type/ref}
                       :publication.translation/lang        {:db/unique :db.unique/identity}
                       :publication.translation/tags        {:db/cardinality :db.cardinality/many}})

(defn prepare-initial-params [params]
  (merge {:publication/state :active}
         params))

(defn append-errors [errors]
  (-> errors
      (errors/attributes '[[?e :publication.translation/state :published]
                           [?e :publication.translation/publication ?root]
                           [?root :publication/state :active]]
                         [[:publication.translation/title        not-empty {:type :must-be-filled}]
                          [:publication.translation/summary      not-empty {:type :must-be-filled}]
                          [:publication.translation/published-at some?     {:type :must-be-filled}]])))
