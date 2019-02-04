(ns publicator-ext.domain.aggregates.author
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.languages :as langs]
   [publicator-ext.util :as u]))

(def ^:const +states+ #{:active :archived})
(def ^:const +stream-participation-roles+ #{:regular :admin})

(defmethod aggregate/schema :author [_]
  {:author.translation/author          {:db/valueType :db.type/ref}
   :author.stream-participation/author {:db/valueType :db.type/ref}})

(defmethod aggregate/validator :author [chain]
  (-> chain
      (validation/attributes '{:find  [[?e ...]]
                               :where [[?e :db/ident :root]]}
                             [[:req :author/state +states+]])
      (validation/attributes '{:find  [[?e ...]]
                               :where [[?e :author.translation/author :root]]}
                             [[:req :author.translation/lang langs/+languages+]
                              [:req :author.translation/first-name string?]
                              [:req :author.translation/first-name not-empty]
                              [:req :author.translation/last-name string?]
                              [:req :author.translation/last-name not-empty]])
      (validation/attributes '{:find  [[?e ...]]
                               :where [[?e :author.stream-participation/author :root]]}
                             [[:req :author.stream-participation/role +stream-participation-roles+]
                              [:req :author.stream-participation/stream-id pos-int?]])
      (validation/query '{:find  [[?e ...]]
                          :where [[?e :db/ident :root]]}
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :author.translation/author ?e]
                                  [?trans :author.translation/lang ?lang]]}
                        u/match? langs/+languages+)
      (validation/query '{:find  [[?e ...]]
                          :where [[?e :db/ident :root]]}
                        '{:find  [[?stream-id ...]]
                          :in    [$ ?e]
                          :with  [?part]
                          :where [[?part :author.stream-participation/author ?e]
                                  [?part :author.stream-participation/stream-id ?stream-id]]}
                        u/distinct?)))

(defn build [user-id tx-data]
  (aggregate/build :author user-id tx-data))
