(ns publicator.persistence.types
  (:require
   [next.jdbc.prepare :as prepare]
   [next.jdbc.result-set :as result-set])
  (:import
   [java.sql Array PreparedStatement]))

(set! *warn-on-reflection* true)

(extend-protocol result-set/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))

(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (into-array v))))
