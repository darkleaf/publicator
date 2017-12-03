(ns publicator.impl.test-db
  (:require
   [jdbc.core :as jdbc]
   [hugsql.core :as hugsql]
   [publicator.db.migration :as migration])
  (:import
   [com.mchange.v2.c3p0 ComboPooledDataSource]))

(def data-source
  (doto (ComboPooledDataSource.)
    (.setJdbcUrl "jdbc:postgresql://db/test")
    (.setUser "postgres")
    (.setPassword "password")))

(migration/migrate data-source)

;; ~~~~~~~~~~~~~~~~

(hugsql/def-db-fns "publicator/impl/test_db.sql" {:quoting :ansi})

(defn clear-fixture [t]
  (try
    (t)
    (finally
      (with-open [conn (jdbc/connection data-source)]
        (truncate-all conn)))))
