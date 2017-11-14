(ns publicator.impl.test-db
  (:require
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql])
  (:import
   [com.mchange.v2.c3p0 ComboPooledDataSource]))

(def data-source
  (doto (ComboPooledDataSource.)
    (.setJdbcUrl "jdbc:postgresql://db/test")
    (.setUser "postgres")
    (.setPassword "password")))

(hugsql/def-db-fns "publicator/impl/test_db.sql" {:quoting :ansi})

(defn truncate-all []
  (with-open [conn (jdbc/connection data-source)]
    (sql-truncate-all conn)))