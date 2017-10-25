(ns publicator.components.implementations
  (:require
   [com.stuartsierra.component :as component]
   [publicator.fakes.storage :as storage]
   [publicator.fakes.user-queries :as user-q]
   [publicator.fakes.post-queries :as post-q]
   [publicator.fakes.hasher :as hasher]
   [publicator.fakes.id-generator :as id-generator]))

(defrecord Impl [db binding-map]
  component/Lifecycle
  (start [this]
    (assoc this :binding-map
           (reduce merge [(storage/binding-map (:conn db))
                          (user-q/binging-map (:conn db))
                          (post-q/binging-map (:conn db))
                          (hasher/binding-map)
                          (id-generator/binging-map)])))
  (stop [this]
    (assoc this :binding-map nil)))

(defn build []
  (Impl. nil nil))
