(ns publicator.fakes.storage
  "Storage with fake transactions.
   No isolation, no rollback."
  (:require
   [publicator.interactors.abstractions.storage :as storage]))

(deftype AggregateBox [volatile id]
  clojure.lang.IDeref
  (deref [_] @volatile)

  storage/AggregateBox
  (-set! [_ new] (vreset! volatile new))
  (-id [_] id)
  (-version [_] nil))

(defn- build-box [state id]
  (AggregateBox. (volatile! state) id))

(deftype Transaction [db]
  storage/Transaction
  (-get-many [_ ids]
    (->> ids
         (map #(get @db % (build-box nil %)))
         (remove nil?)))
  (-create [_ state]
    (let [id  (:id state)
          box (build-box state id)]
      (swap! db assoc id box)
      box)))

(deftype Storage [db]
  storage/Storage
  (-wrap-tx [_ body]
    (let [t (Transaction. db)]
      (body t))))

(defn build-db []
  (atom {}))

(defn binding-map [db]
  {#'storage/*storage* (->Storage db)})
