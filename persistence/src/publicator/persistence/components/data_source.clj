(ns publicator.persistence.components.data-source
  (:require
   [com.stuartsierra.component :as component])
  (:import
   [com.mchange.v2.c3p0 ComboPooledDataSource]))

(defrecord DataSource [config val]
  component/Lifecycle
  (start [this]
    (assoc this :val
           (doto (ComboPooledDataSource.)
             (.setJdbcUrl (:jdbc-url config))
             (.setUser (:user config))
             (.setPassword (:password config)))))
  (stop [this]
    (.close val)
    (assoc this :val nil)))

(defn build [config]
  (DataSource. config nil))
