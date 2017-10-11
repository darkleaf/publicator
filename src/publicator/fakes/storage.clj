(ns publicator.fakes.storage
  "Thread unsafe fake storage"
  (:require
   [publicator.interactors.abstractions.storage :as storage]))

(deftype Aggregate [volatile]
  clojure.lang.IDeref
  (deref [_]  @volatile)

  storage/Aggregate
  (-update-agg! [this f args]
    (let [new (apply f @volatile args)]
      (vreset! volatile new)
      new)))

(defn aggregate [state]
  (Aggregate. (volatile! state)))

(deftype Transaction [db]
  storage/Transaction

  (-get-aggs [_ ids]
    (map #(get @db %) ids))

  (-create-agg [_ state]
    (let [id  (:id state)
          agg (aggregate state)]
      (vswap! db assoc id agg)
      agg)))

(deftype Storage [db]
  storage/Storage

  (-tx [_ body]
    (binding [storage/*tx* (Transaction. db)]
      (body))))

(defn build-db []
  (volatile! {}))

(defn build-storage [db]
  (Storage. db))
