(ns publicator.persistence.handlers
  (:require
   [datascript.core :as d]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as jdbc.sql]
   [publicator.core.domain.aggregate :as agg]
   [publicator.persistence.serialization :as serialization]
   [publicator.persistence.settings :as settings]))

(defn- user-get-by-id [tx id]
  (some-> (jdbc.sql/get-by-id tx "user" id "agg/id" {})
          (serialization/row->agg)))

#_(defn- user-create [{::keys [tx]} user]
    (let [row (serialization/agg->row user)
          row (jdbc.sql/insert! tx "user" row opts)]
      (serialization/row->agg row)))

(defn handlers [tx]
  {:persistence.user/get-by-id  (partial user-get-by-id tx)
   #_#_:persistence.user/create (partial user-create tx)})

(defmacro with-handlers [[handlers transactable] & body]
  `(jdbc/with-transaction [tx# ~transactable]
     (let [tx#       (jdbc/with-options tx# settings/opts)
           ~handlers (handlers tx#)]
       ~@body)))
