(ns publicator.use-cases.abstractions.storage-fake
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as aggregate]))

(defn- build-atomic-apply [db]
  (fn [func]
    (locking db
      (let [t (reify
                storage/Transaction
                (get-many [_ ids]
                  (select-keys @db ids))
                (create [_ state]
                  (let [id   (-> state aggregate/root :aggregate/id)
                        iagg (ref state)]
                    (vswap! db assoc id iagg)
                    iagg)))]
        (func t)))))

(defn build-db []
  (volatile! {}))

(defn binding-map [db]
  {#'storage/*atomic-apply* (build-atomic-apply db)})
