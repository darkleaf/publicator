(ns publicator.persistence.components.migration
  (:require
   [com.stuartsierra.component :as component])
  (:import
   [org.flywaydb.core Flyway]))

(defrecord Migration [data-source]
  component/Lifecycle
  (start [this]
    (doto (Flyway.)
      (.setDataSource (:val data-source))
      (.migrate))
    this)
  (stop [this]
    this))

(defn build []
  (Migration. nil))
