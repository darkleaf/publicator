(ns publicator.components.impl
  (:require
   [com.stuartsierra.component :as component]

   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fake.user-q]))

(defrecord Impl [db impl]
  component/Lifecycle
  (start [this]
    (-> this
        (assoc-in [:impl ::storage/storage]
                  (fakes.storage/build-storage (:conn db)))
        (assoc-in [:impl ::user-q/get-by-login]
                  (fake.user-q/build-get-by-login (:conn db)))))
  (stop [this]
    (assoc this :impl {})))

(defn build []
  (->Impl nil {}))
