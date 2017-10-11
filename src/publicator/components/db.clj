(ns publicator.components.db
  (:require
   [com.stuartsierra.component :as component]
   [publicator.fakes.storage :as fakes.storage]))

(defrecord Db [conn]
  component/Lifecycle
  (start [this]
    (assoc this :conn (fakes.storage/build-db)))
  (stop [this]
    (assoc this :conn nil)))

(defn build []
  (->Db nil))
