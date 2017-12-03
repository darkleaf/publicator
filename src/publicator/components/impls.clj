(ns publicator.components.impls
  (:require
   [com.stuartsierra.component :as component]
   [publicator.impl.hasher :as hasher]
   [publicator.impl.id-generator :as id-generator]
   [publicator.impl.post-queries :as post-q]
   [publicator.impl.storage :as storage]
   [publicator.impl.storage.post-manager :as post-manager]
   [publicator.impl.storage.user-manager :as user-manager]
   [publicator.impl.user-queries :as user-q]))

(defrecord Impls [data-source binding-map]
  component/Lifecycle
  (start [this]
    (let [data-source (:data-source data-source)
          managers    (merge
                       (post-manager/manager)
                       (user-manager/manager))
          binding-map (merge
                       (storage/binding-map data-source managers)
                       (user-q/binding-map data-source)
                       (post-q/binding-map data-source)
                       (hasher/binding-map)
                       (id-generator/binding-map data-source))]
      (assoc this :binding-map binding-map)))
  (stop [this]
    (assoc this :binding-map nil)))

(defn build []
  (Impls. nil nil))
