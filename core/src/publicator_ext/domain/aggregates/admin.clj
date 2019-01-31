(ns publicator-ext.domain.aggregates.admin
  (:require
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.abstractions.instant :as instant]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.util.validation :as validation]))

(def ^:const +states+ #{:active :archived})

(defmethod aggregate/validator :admin [chain]
  (-> chain
      (validation/attributes '[[(entity ?e)
                                [?e :db/ident :root]]]
                             [[:req :admin/state +states+]])))

(defn build [user-id tx-data]
  (aggregate/build :admin user-id tx-data))
