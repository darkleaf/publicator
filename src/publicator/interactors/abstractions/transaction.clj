(ns publicator.interactors.abstractions.transaction
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol TxFactory
  (build [this]))

(s/def ::tx-factory #(satisfies? TxFactory %))

(defprotocol Transaction
  (get-aggregates [this klass ids])
  (create-aggregate [this state])
  (wrap [this body]))

(s/def ::transaction #(satisfies? Transaction %))

(defmacro with-tx [binding & body]
  (assert (= 2 (count binding)))
  (let [[sym form] binding]
    `(wrap ~form (fn [~sym] ~@body))))

(defn get-aggregate [this klass id]
  (first (get-aggregates this klass [id])))
