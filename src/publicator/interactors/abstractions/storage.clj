(ns publicator.interactors.abstractions.storage)

(defprotocol Storage
  (wrap-tx [this body]))

(defprotocol Transaction
  (get-aggs [this ids])
  (create-agg [this state]))

(declare ^:dynamic *storage*)

(defmacro with-tx [tx-name & body]
  `(wrap-tx *storage* (fn [~tx-name] ~@body)))

(defn get-agg [t id]
  (first (get-aggs t [id])))

(defn tx-get [id]
  (with-tx t
    @(get-agg t id)))

(defn tx-create [state]
  (with-tx t
    @(create-agg t state)))
