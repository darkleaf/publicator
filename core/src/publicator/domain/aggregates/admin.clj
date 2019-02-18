(ns publicator.domain.aggregates.admin
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.abstractions.instant :as instant]
   [publicator.domain.aggregate :as aggregate]
   [publicator.domain.utils.validation :as validation]))

(def ^:const +states+ #{:active :archived})

(defmethod aggregate/validator :admin [chain]
  (-> chain
      (validation/types [:admin/state +states+])
      (validation/required-for aggregate/root-q
                               [:admin/state some?])))

(defn build [user-id tx-data]
  (aggregate/build :admin user-id tx-data))
