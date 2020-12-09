(ns publicator.persistence.jdbc-options
  (:require
   [next.jdbc.quoted :as quoted]
   [next.jdbc.result-set :as result-set])
  (:import
   [java.sql ResultSet ResultSetMetaData]))

(set! *warn-on-reflection* true)

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
