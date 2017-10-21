(ns publicator.fakes.storage
  "Storage with fake transactions.
   No isolation, no rollback."
  (:require
   [publicator.interactors.abstractions.storage :as storage]))

(deftype Transaction [db]
  storage/Transaction

  (get-aggs [_ ids]
    (map #(get @db %) ids))

  (create-agg [_ state]
    (let [id  (:id state)
          agg (atom state)]
      (swap! db assoc id agg)
      agg)))

(deftype Storage [db]
  storage/Storage

  (wrap-tx [_ body]
    (let [t (Transaction. db)]
      (body t))))

(defn build-db []
  (atom {}))

(defn build-storage [db]
  (Storage. db))

(defn binding-map [db]
  {#'storage/*storage* (build-storage db)})
