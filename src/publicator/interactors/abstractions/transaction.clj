(ns publicator.interactors.abstractions.transaction)

(defprotocol PTxFactory
  (build [this]))

(defprotocol PTransaction
  (get-aggregates [this klass ids])
  (create-aggregate [this state])
  (wrap [this body]))

(defmacro with-tx [binding & body]
  (assert (= 2 (count binding)))
  (let [[sym form] binding]
    `(wrap ~form (fn [~sym] ~@body))))

(defn get-aggregate [this klass id]
  (first (get-aggregates this klass [id])))
