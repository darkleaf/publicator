(ns publicator-ext.domain.aggregates.user
  (:require
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.abstractions.instant :as instant]
   [publicator-ext.domain.aggregate :as aggregate]
   [publicator-ext.domain.util.validation :as validation]))

(def ^:const +states+ #{:active :archived})

(defmethod aggregate/validator :user [chain]
  (-> chain
      (validation/types [:user/login string?]
                        [:user/password-digest string?]
                        [:user/state +states+])

      (validation/required-for aggregate/root-q
                               [:user/login not-empty]
                               [:user/password-digest not-empty]
                               [:user/state some?])))

(defn build [tx-data]
  (let [id (id-generator/generate :user)]
    (aggregate/build :user id tx-data)))
