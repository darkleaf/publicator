(ns publicator.fakes.storage
  "Thread unsafe fake storage"
  (:require
   [publicator.interactors.abstractions.transaction :as tx]
   [publicator.interactors.utils.aggregate :as aggregate]))

(deftype FakeTransaction [db]
  tx/PTransaction
  (get-aggregates [_ klass ids]
    (map #(get @db %) ids))

  (create-aggregate [_ state]
    (let [agg (aggregate/build state)
          id  (:id @agg)
          _   (vswap! db assoc id agg)]
      agg))

  (wrap [this body]
    (body this)))

(deftype FakeTxFactory [db]
  tx/PTxFactory
  (build [_]
    (->FakeTransaction db)))

(defn build-tx-factory []
  (->FakeTxFactory (volatile! {})))
