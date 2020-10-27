(ns publicator.persistence.handlers
  (:require
   [datascript.core :as d]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as jdbc.sql]
   [next.jdbc.quoted :as jdbc.quoted]
   [next.jdbc.result-set :as jdbc.rs]
   [next.jdbc.prepare :as jdbc.prepare]
   [publicator.core.domain.aggregate :as agg]
   [publicator.persistence.serialization :as serialization]
   [publicator.utils :as u])
  (:import
   [java.sql Array PreparedStatement ResultSet ResultSetMetaData]))

(extend-protocol jdbc.rs/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))

(extend-protocol jdbc.prepare/SettableParameter
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
    (jdbc.rs/->MapResultSetBuilder rs rsmeta cols)))

(def opts {:table-fn   jdbc.quoted/postgres
           :column-fn  jdbc.quoted/postgres
           :builder-fn as-str-maps})

(defn wrap-tx [perform transactable]
  (fn [handlers continuation [ctx args]]
    (jdbc/with-transaction [tx transactable]
      (let [ctx (assoc ctx ::tx tx)]
        (perform handlers continuation [ctx args])))))

(defn- wrap-context-reader [handler]
  (fn [context & args]
    [context (apply handler context args)]))

(defn- user-get-by-id [{::keys [tx]} id]
  (some-> (jdbc.sql/get-by-id tx "user" id "r:agg/id" opts)
          (serialization/row->agg)))

(defn- user-create [{::keys [tx]} user]
  (let [row (serialization/agg->row user)
        row (jdbc.sql/insert! tx "user" row opts)]
    (serialization/row->agg row)))

(defn handlers []
  {:persistence.user/get-by-id (-> user-get-by-id (wrap-context-reader))
   :persistence.user/create    (-> user-create (wrap-context-reader))})


;; локализацию нужно хранить плоско - ru_title, ru_content и т.д.
