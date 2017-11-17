(ns publicator.components.data-source
  (:require
   [com.stuartsierra.component :as component])
  (:import
   [com.mchange.v2.c3p0 ComboPooledDataSource]))

(defn- build-pool [{:keys [jdbc-url user password]}]
  {:pre [(every? string? [jdbc-url user password])]}
  (doto (ComboPooledDataSource.)
    (.setJdbcUrl jdbc-url)
    (.setUser user)
    (.setPassword password)))

(defrecord DataSource [config data-source]
  component/Lifecycle
  (start [this]
    (assoc this :data-source (build-pool config)))
  (stop [this]
    (assoc this :data-source nil)))

(defn build [config]
  (DataSource. config nil))
