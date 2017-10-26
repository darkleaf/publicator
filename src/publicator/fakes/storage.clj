(ns publicator.fakes.storage
  "Storage with fake transactions.
   No isolation, no rollback."
  (:require
   [publicator.interactors.abstractions.storage :as storage]))

(deftype Transaction [db]
  storage/Transaction
  (-get-many [_ ids]
    (->> ids
         (map #(get @db %))
         (remove nil?)))
  (-create [_ state]
    (let [id  (:id state)
          agg (atom state)]
      (swap! db assoc id agg)
      agg)))

(deftype Storage [db]
  storage/Storage
  (-wrap-tx [_ body]
    (let [t (Transaction. db)]
      (body t))))

(defn build-db []
  (atom {}))

(defn binding-map [db]
  {#'storage/*storage* (->Storage db)})
