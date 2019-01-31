(ns publicator-ext.domain.aggregates.author
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.languages :as langs]))

(def ^:const +states+ #{:active :archived})
(def ^:const +stream-participation-roles+ #{:regular :admin})

(defmethod aggregate/schema :author [_]
  {:author.translation/lang               {:db/unique :db.unique/identity}
   :author.translation/author             {:db/valueType :db.type/ref}
   :author.stream-participation/stream-id {:db/unique :db.unique/identity}
   :author.stream-participation/author    {:db/valueType :db.type/ref}})

(defmethod aggregate/validator :author [chain]
  (-> chain
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:req :author/state +states+]])
      (validation/attributes '[[(entity ?e)
                                [?e :author.translation/author :root]]]
                             [[:req :author.translation/lang langs/+languages+]
                              [:req :author.translation/first-name string?]
                              [:req :author.translation/first-name not-empty]
                              [:req :author.translation/last-name string?]
                              [:req :author.translation/last-name not-empty]])
      (validation/attributes '[[(entity ?e)
                                [?e :author.stream-participation/author :root]]]
                             [[:req :author.stream-participation/role +stream-participation-roles+]
                              [:req :author.stream-participation/stream-id pos-int?]])))

(defn build [user-id tx-data]
  (aggregate/build :author user-id tx-data))
