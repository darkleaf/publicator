(ns publicator.persistence.components
  (:require
   [com.stuartsierra.component :as component]
   [hikari-cp.core :as hikari-cp])
  (:import
   [javax.sql DataSource]
   [org.flywaydb.core Flyway]
   [org.jdbi.v3.core Jdbi]
   [org.jdbi.v3.postgres PostgresPlugin]))

(defprotocol DataSourceProvider
  (get-datasource ^DataSource [this]))

(defrecord HikariCP [datasource options]
  DataSourceProvider
  (get-datasource [_] datasource)

  component/Lifecycle
  (start [this]
    (assoc this :datasource (hikari-cp/make-datasource options)))
  (stop [this]
    (hikari-cp/close-datasource datasource)
    (assoc this :datasource nil)))

(defn hikari-cp [options]
  (->HikariCP nil options))

(defrecord Migration [datasource-provider]
  component/Lifecycle
  (start [this]
    (.. Flyway
        (configure)
        (dataSource (get-datasource datasource-provider))
        (load)
        (migrate))
    this)
  (stop [this] this))

(defn migration []
  (->Migration nil))

(defprotocol JdbiProvider
  (get-jdbi ^Jdbi [this]))

(defrecord JdbiComponent [jdbi datasource-provider]
  JdbiProvider
  (get-jdbi [_] jdbi)

  component/Lifecycle
  (start [this]
    (assoc this :jdbi (.. Jdbi
                          (create (get-datasource datasource-provider))
                          (installPlugin (PostgresPlugin.)))))
  (stop [this]
    (assoc this :jdbi nil)))
