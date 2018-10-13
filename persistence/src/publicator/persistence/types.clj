(ns publicator.persistence.types
  (:require
   [jdbc.proto])
  (:import
   [org.postgresql.jdbc PgArray]
   [org.postgresql.util PGobject]
   [java.sql Timestamp]
   [java.time Instant]
   [java.util Collection]))

(extend-protocol jdbc.proto/ISQLResultSetReadColumn
  PGobject
  (from-sql-type [this _conn _metadata _i]
    (let [type  (.getType this)
          value (.getValue this)]
      (case type
        "xid" (bigint value)
        :else this)))

  PgArray
  (from-sql-type [this _conn metadata i]
    (let [column-name (.getColumnName metadata i)
          arr         (.getArray this)]
      (cond
        (re-matches #".+-ids" column-name) (set arr)
        :else                              (vec arr))))

  Timestamp
  (from-sql-type [this _conn _metadata _i]
    (.toInstant this)))

(extend-protocol jdbc.proto/ISQLType
  Instant
  (set-stmt-parameter! [self conn stmt index]
    (let [sql-val (Timestamp/from self)]
      (.setObject stmt index sql-val)))

  Collection
  (set-stmt-parameter! [self conn stmt index]
    (let [scalar-type (-> stmt
                          .getParameterMetaData
                          (.getParameterTypeName index)
                          (subs 1))
          sql-val     (.createArrayOf conn scalar-type (to-array self))]
      (.setObject stmt index sql-val))))
