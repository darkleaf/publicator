(ns publicator.use-cases.abstractions.test-impl.storage-fake
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as agg]))

(defn build-db []
  (atom {}))

(defn- ->create [db]
  (fn [agg]
    (let [id       (agg/id agg)
          agg-type (agg/type agg)
          iagg     (ref agg)]
      (swap! db assoc-in [agg-type id] iagg)
      iagg)))

(defn- ->preload [db]
  (fn [type ids]
    nil))

(defn- ->get [db]
  (fn [agg-type id]
    (get-in @db [agg-type id])))

;; todo: collect errors
;; todo: move to storage abstraction?
(defn- validate! [db]
  (let [aggs (->> @db
                  (vals)
                  (mapcat vals)
                  (map deref))]
    (doseq [agg aggs]
      (agg/validate! agg))))

(defn- ->transaction [db]
  (fn [func]
    (locking db
      (binding [storage/*create*  (->create db)
                storage/*preload* (->preload db)
                storage/*get*     (->get db)]
        (let [res (func)]
          (validate! db)
          res)))))

(defn binding-map [db]
  {#'storage/*transaction* (->transaction db)})
