(ns publicator.use-cases.abstractions.storage-fake
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as aggregate]))

(defn- get-many [t ids]
  (select-keys @t ids))

(defn- create [t state]
  (let [id   (-> state aggregate/root :root/id)
        iagg (ref state)]
    (swap! t assoc id iagg)
    iagg))

(defn- build-atomic-apply [db]
  (fn [func]
    (locking db
      (func db))))

(defn build-db []
  (atom {} :meta {`storage/create   create
                  `storage/get-many get-many}))

(defn binding-map [db]
  {#'storage/*atomic-apply* (build-atomic-apply db)})
