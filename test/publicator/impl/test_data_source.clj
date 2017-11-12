(ns publicator.impl.test-data-source
  (:import
   [com.mchange.v2.c3p0 ComboPooledDataSource]))

(def data-source
  (doto (ComboPooledDataSource.)
    (.setJdbcUrl "jdbc:postgresql://db/test")
    (.setUser "postgres")
    (.setPassword "password")))
