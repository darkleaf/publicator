(ns publicator.persistence.handlers
  (:require
   [datascript.core :as d]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as jdbc.sql]
   [publicator.core.domain.aggregate :as agg]
   [publicator.persistence.jdbc-options :as jdbc-options]
   [publicator.persistence.serialization :as serialization]
   [publicator.persistence.types]))

(defn- user-get-by-id [tx id]
  (some-> (jdbc.sql/get-by-id tx "user" id "agg/id" {})
          (serialization/row->agg)))

(defn- user-create [tx user]
  (let [row (serialization/agg->row user)
        row (jdbc.sql/insert! tx "user" row)]
    (serialization/row->agg row)))

(defn- publication-get-by-id [tx id]
  (some-> (jdbc.sql/get-by-id tx "publication" id "agg/id" {})
          (serialization/row->agg)))

(defn handlers [tx]
  {:persistence.user/get-by-id        (partial user-get-by-id tx)
   :persistence.user/create           (partial user-create tx)
   :persistence.publication/get-by-id (partial publication-get-by-id tx)})

(defmacro with-handlers [[handlers transactable] & body]
  `(jdbc/with-transaction [tx# ~transactable]
     (let [tx#       (jdbc/with-options tx# jdbc-options/opts)
           ~handlers (handlers tx#)]
       ~@body)))
