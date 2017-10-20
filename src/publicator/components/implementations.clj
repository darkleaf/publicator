(ns publicator.components.implementations
  (:require
   [com.stuartsierra.component :as component]

   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fakes.user-q]

   [publicator.domain.abstractions.hasher :as hasher]
   [publicator.fakes.hasher :as fakes.hasher]))

(defrecord Impl [db binding-map]
  component/Lifecycle
  (start [this]
    (assoc this :binding-map
           {#'storage/*storage*     (fakes.storage/build-storage (:conn db))
            #'user-q/*get-by-login* (fakes.user-q/build-get-by-login (:conn db))
            #'hasher/*hasher*       (fakes.hasher/build)}))
  (stop [this]
    (assoc this :binding-map nil)))

(defn build []
  (Impl. nil nil))
