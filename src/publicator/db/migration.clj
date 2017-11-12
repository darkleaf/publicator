(ns publicator.db.migration
  (:import
   [org.flywaydb.core Flyway]))

(defn migrate [data-source]
  (doto (Flyway.)
    (.setDataSource data-source)
    (.migrate)))
