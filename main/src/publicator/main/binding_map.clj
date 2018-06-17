(ns publicator.main.binding-map
  (:require
   [com.stuartsierra.component :as component]
   [publicator.persistence.storage :as storage]
   [publicator.persistence.storage.user-mapper :as user-mapper]
   [publicator.persistence.storage.post-mapper :as post-mapper]
   [publicator.persistence.user-queries :as user-q]
   [publicator.persistence.post-queries :as post-q]
   [publicator.persistence.id-generator :as id-generator]
   [publicator.crypto.password-hasher :as password-hasher]))

(defrecord BindingMap [data-source val]
  component/Lifecycle
  (start [this]
    (let [data-source (:val data-source)
          mappers     (merge
                       (post-mapper/mapper)
                       (user-mapper/mapper))
          binding-map (merge
                       (storage/binding-map data-source mappers)
                       (user-q/binding-map data-source)
                       (post-q/binding-map data-source)
                       (password-hasher/binding-map)
                       (id-generator/binding-map data-source))]
      (assoc this :val binding-map)))
  (stop [this]
    (assoc this :val nil)))

(defn build []
  (BindingMap. nil nil))
