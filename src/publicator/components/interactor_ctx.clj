(ns publicator.components.interactor-ctx
  (:require
   [com.stuartsierra.component :as component]

   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fake.user-q]))

(defrecord Ctx [db ctx]
  component/Lifecycle
  (start [this]
    (assoc this :ctx
           {::storage/storage     (fakes.storage/build-storage (:conn db))
            ::user-q/get-by-login (fake.user-q/build-get-by-login (:conn db))}))
  (stop [this]
    (assoc this :impl nil)))

(defn build []
  (Ctx. nil nil))
