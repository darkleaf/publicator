(ns publicator.persistence.test.db
  (:require
   [publicator.persistence.components.data-source :as data-source]
   [publicator.persistence.components.migration :as migration]
   [com.stuartsierra.component :as component]
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]))

(hugsql/def-db-fns "publicator/persistence/test/db.sql"
  {:adapter (cj-adapter/hugsql-adapter-clojure-jdbc)
   :quoting :ansi})

(defn- build-system []
  (component/system-map
   :data-source (data-source/build {:jdbc-url (str "jdbc:postgresql://db/test")
                                    :user "postgres"
                                    :password "password"})
   :migration (component/using (migration/build)
                               [:data-source])))

(defn- with-system [f]
  (let [system (atom (build-system))]
    (try
      (swap! system component/start)
      (f @system)
      (finally
        (swap! system component/stop)))))

(defn- create-test-db []
  (with-open [conn (jdbc/connection "postgresql://postgres:password@db/")]
    (when (nil? (jdbc/fetch-one conn "select 1 from pg_database where datname = 'test'"))
      (jdbc/execute conn "create database test"))))

(declare ^:dynamic *data-source*)

(defn once-fixture [t]
  (create-test-db)
  (with-system
    (fn [system]
      (let [data-source (-> system :data-source :val)]
        (binding [*data-source* data-source]
          (t))))))

(defn each-fixture [t]
  (try
    (t)
    (finally
      (with-open [conn (jdbc/connection *data-source*)]
        (truncate-all conn)))))
