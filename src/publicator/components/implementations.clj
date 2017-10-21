(ns publicator.components.implementations
  (:require
   [com.stuartsierra.component :as component]
   [publicator.fakes.storage :as fakes.storage]
   [publicator.fakes.user-queries :as fakes.user-q]
   [publicator.fakes.hasher :as fakes.hasher]
   [publicator.fakes.id-generator :as fakes.id-generator]))

(defrecord Impl [db binding-map]
  component/Lifecycle
  (start [this]
    (assoc this :binding-map
           (reduce merge [(fakes.storage/binding-map (:conn db))
                          (fakes.user-q/binging-map (:conn db))
                          (fakes.hasher/binding-map)
                          (fakes.id-generator/binging-map)])))



  (stop [this]
    (assoc this :binding-map nil)))

(defn build []
  (Impl. nil nil))
