(ns publicator.components.db-pool
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

(defrecord DbPool [pool config]
  component/Lifecycle
  (start [this]
    (assoc this :pool (build-pool config)))
  (stop [this]
    (assoc this :pool nil)))

(defn build []
  (DbPool. nil nil))
