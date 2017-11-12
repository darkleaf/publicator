(ns publicator.components.fake-impl
  (:require
   [com.stuartsierra.component :as component]
   [publicator.fakes.storage :as storage]
   [publicator.fakes.user-queries :as user-q]
   [publicator.fakes.post-queries :as post-q]
   [publicator.fakes.hasher :as hasher]
   [publicator.fakes.id-generator :as id-generator]))

(defrecord Impl [binding-map]
  component/Lifecycle
  (start [this]
    (let [db          (storage/build-db)
          binding-map (merge
                       (storage/binding-map db)
                       (user-q/binging-map db)
                       (post-q/binging-map db)
                       (hasher/binding-map)
                       (id-generator/binging-map))]
      (assoc this :binding-map binding-map)))
  (stop [this]
    (assoc this :binding-map nil)))

(defn build []
  (Impl. nil))
