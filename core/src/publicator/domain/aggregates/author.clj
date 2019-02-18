(ns publicator.domain.aggregates.author
  (:require
   [publicator.domain.aggregate :as aggregate]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.util.validation :as validation]
   [publicator.domain.languages :as langs]
   [publicator.utils.coll :as u.c]))

(def ^:const +states+ #{:active :archived})
(def ^:const +stream-participation-roles+ #{:regular :admin})

(def ^:const translations-q
  '{:find  [[?e ...]]
    :where [[?e :author.translation/author :root]]})

(def ^:const stream-participations-q
  '{:find  [[?e ...]]
    :where [[?e :author.stream-participation/author :root]]})

(defmethod aggregate/schema :author [_]
  {:author.translation/author          {:db/valueType :db.type/ref}
   :author.stream-participation/author {:db/valueType :db.type/ref}})

(defmethod aggregate/validator :author [chain]
  (-> chain
      (validation/types [:author/state +states+]
                        [:author.translation/lang langs/+languages+]
                        [:author.translation/first-name string?]
                        [:author.translation/last-name string?]
                        [:author.stream-participation/role +stream-participation-roles+]
                        [:author.stream-participation/stream-id pos-int?])

      (validation/required-for aggregate/root-q
                               [:author/state some?])
      (validation/required-for translations-q
                               [:author.translation/lang some?]
                               [:author.translation/first-name not-empty]
                               [:author.translation/last-name not-empty])
      (validation/required-for stream-participations-q
                               [:author.stream-participation/role some?]
                               [:author.stream-participation/stream-id some?])

      (validation/query aggregate/root-q
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :author.translation/author ?e]
                                  [?trans :author.translation/lang ?lang]]}
                        u.c/match? langs/+languages+)
      (validation/query aggregate/root-q
                        '{:find  [[?stream-id ...]]
                          :in    [$ ?e]
                          :with  [?part]
                          :where [[?part :author.stream-participation/author ?e]
                                  [?part :author.stream-participation/stream-id ?stream-id]]}
                        u.c/distinct?)))

(defn build [user-id tx-data]
  (aggregate/build :author user-id tx-data))
