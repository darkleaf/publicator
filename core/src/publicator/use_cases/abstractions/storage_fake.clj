(ns publicator.use-cases.abstractions.storage-fake
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as aggregate]))

(defn- build-atomic-apply [db]
  (fn [func]
    (locking db
      (let [t db]
        (func t)))))

(defn- get-many [t ids]
  (select-keys @t ids))

(defn- create [t state]
  (let [id   (-> state aggregate/root :aggregate/id)
        iagg (ref state)]
    (vswap! t assoc id iagg)
    iagg))

(defn build-db []
  (volatile! {}))

(defn binding-map [db]
  {#'storage/atomic-apply (build-atomic-apply db)
   #'storage/get-many     get-many
   #'storage/create       create})
