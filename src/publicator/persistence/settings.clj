(ns publicator.persistence.settings
  (:require
   [next.jdbc.prepare :as prepare]
   [next.jdbc.quoted :as quoted]
   [next.jdbc.result-set :as result-set])
  (:import
   [java.sql Array PreparedStatement ResultSet ResultSetMetaData]))

(set! *warn-on-reflection* true)

(extend-protocol result-set/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))

(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (into-array v))))

(defn- get-str-column-names
  [^ResultSetMetaData rsmeta _]
  (mapv (fn [^Integer i]
          (.getColumnLabel rsmeta i))
        (range 1 (inc (.getColumnCount rsmeta)))))

(defn- as-str-maps
  [^ResultSet rs opts]
  (let [rsmeta (.getMetaData rs)
        cols   (get-str-column-names rsmeta opts)]
    (result-set/->MapResultSetBuilder rs rsmeta cols)))

(def opts {:table-fn   quoted/postgres
           :column-fn  quoted/postgres
           :builder-fn as-str-maps})
