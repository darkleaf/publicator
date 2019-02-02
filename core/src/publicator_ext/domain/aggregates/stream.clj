(ns publicator-ext.domain.aggregates.stream
  (:require
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.util.validation :as validation]
   [publicator-ext.domain.languages :as langs]))

(def ^:const +states+ #{:active :archived})

(defmethod aggregate/schema :stream [_]
  {:stream.translation/stream {:db/valueType :db.type/ref}
   :stream.translation/lang   {:db/unique :db.unique/identity}})

(defmethod aggregate/validator :stream [chain]
  (-> chain
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:req :stream/state +states+]])
      (validation/attributes '[[(entity ?e)
                                [?e :stream.translation/stream :root]]]
                             [[:req :stream.translation/lang langs/+languages+]
                              [:req :stream.translation/name string?]
                              [:req :stream.translation/name not-empty]])))

(defn build [tx-data]
  (let [id (id-generator/generate :stream)]
    (aggregate/build :stream id tx-data)))
