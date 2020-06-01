(ns publicator.persistence.components
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.protocols :as jdbc.p]
   [com.stuartsierra.component :as component])
  (:import
   [org.flywaydb.core Flyway]
   [com.mchange.v2.c3p0 ComboPooledDataSource]))

(defrecord C3P0 [c3p0 jdbc-url user password]
  component/Lifecycle
  (start [this]
    (assoc this :c3p0
           (doto (ComboPooledDataSource.)
             (.setJdbcUrl jdbc-url)
             (.setUser user)
             (.setPassword password))))
  (stop [this]
    (.close c3p0)
    this)

  jdbc.p/Sourceable
  (get-datasource [_]
    (jdbc.p/get-datasource c3p0)))

(defn c3p0 [jdbc-url user password]
  (->C3P0 nil jdbc-url user password))

(defrecord TestTransactable [connectable]
  jdbc.p/Transactable
  (-transact [_ body-fn opts]
    (jdbc.p/-transact connectable body-fn (assoc opts :rollback-only true))))

(defn test-transactable []
  (->TestTransactable nil))

(defrecord Migration [sourceable]
  component/Lifecycle
  (start [this]
    (.. Flyway
        (configure)
        (dataSource (jdbc.p/get-datasource sourceable))
        (load)
        (migrate))
    this)
  (stop [this] this))

(defn migration []
  (->Migration nil))
