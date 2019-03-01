(ns publicator.use-cases.abstractions.storage-fake
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as aggregate]))

(defn build-db []
  (atom {}))

(defn- ->create [db]
  (fn [state]
    (let [id       (-> state aggregate/root :root/id)
          agg-type (type state)
          iagg     (ref state)]
      (swap! db assoc-in [agg-type id] iagg)
      iagg)))

(defn- ->preload [db]
  (fn [type ids]
    nil))

(defn- ->get [db]
  (fn [agg-type id]
    (get-in @db [agg-type id])))

(defn- ->transaction [db]
  (fn [func]
    (locking db
      (binding [storage/*create*  (->create db)
                storage/*preload* (->preload db)
                storage/*get*     (->get db)]
        (func)))))

(defn binding-map [db]
  {#'storage/*transaction* (->transaction db)})
