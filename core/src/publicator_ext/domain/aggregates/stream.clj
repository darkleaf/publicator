(ns publicator-ext.domain.aggregates.stream
  (:require
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.languages :as langs]
   [publicator-ext.util :as u]))

(def ^:const +states+ #{:active :archived})

(defmethod aggregate/schema :stream [_]
  {:stream.translation/stream {:db/valueType :db.type/ref}})

(defmethod aggregate/validator :stream [chain]
  (-> chain
      (validation/types [:stream/state +states+]
                        [:stream.translation/lang langs/+languages+]
                        [:stream.translation/name string?])

      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :db/ident :root]]}
                               [:stream/state some?])
      (validation/required-for '{:find  [[?e ...]]
                                 :where [[?e :stream.translation/stream :root]]}
                               [:stream.translation/lang some?]
                               [:stream.translation/name not-empty])

      (validation/query '{:find  [[?e ...]]
                          :where [[?e :db/ident :root]]}
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :stream.translation/stream ?e]
                                  [?trans :stream.translation/lang ?lang]]}
                        u/match? langs/+languages+)))

(defn build [tx-data]
  (let [id (id-generator/generate :stream)]
    (aggregate/build :stream id tx-data)))
