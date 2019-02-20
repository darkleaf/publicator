(ns publicator.domain.aggregates.stream
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.aggregate :as aggregate]
   [publicator.domain.utils.validation :as validation]
   [publicator.domain.languages :as langs]
   [publicator.utils.coll :as u.c]))

(def ^:const +states+ #{:active :archived})

(def ^:const translations-q
  '{:find  [[?e ...]]
    :where [[?e :stream.translation/stream :root]]})

(defmethod aggregate/schema :stream [_]
  {:stream.translation/stream {:db/valueType :db.type/ref}})

(defmethod aggregate/validator :stream [chain]
  (-> chain
      (validation/types [:stream/state +states+]
                        [:stream.translation/lang langs/+languages+]
                        [:stream.translation/name string?])

      (validation/required-for aggregate/root-q
                               [:stream/state some?])
      (validation/required-for translations-q
                               [:stream.translation/lang some?]
                               [:stream.translation/name not-empty])

      (validation/query aggregate/root-q
                        '{:find  [[?lang ...]]
                          :in    [$ ?e]
                          :with  [?trans]
                          :where [[?trans :stream.translation/stream ?e]
                                  [?trans :stream.translation/lang ?lang]]}
                        u.c/match? langs/+languages+)))

(defn build [tx-data]
  (let [id (id-generator/*generate* :stream)]
    (aggregate/build :stream id tx-data)))
