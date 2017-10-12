(ns publicator.interactors.abstractions.storage)

(defprotocol Storage
  (-tx [this body]))

(defprotocol Transaction
  (-get-aggs [this ids])
  (-create-agg [this state]))

;; also clojure.lang.IDeref
(defprotocol Aggregate
  (-update-agg! [this f args]))

(defmacro ^{:style/indent :defn} tx [storage & body]
  `(-tx ~storage (fn [] ~@body)))

(declare ^:dynamic *tx*)

(defn get-aggs [ids]
  (-get-aggs *tx* ids))

(defn get-agg [id]
  (first (get-aggs [id])))

(defn create-agg [state]
  (-create-agg *tx* state))

(defn get-agg-from [storage id]
  (tx storage
    (get-agg id)))

(defn create-agg-in [storage state]
  (tx storage
    @(create-agg state)))

(defn update-agg! [agg f & args]
  (-update-agg! agg f args))

(defn reset-agg! [agg state]
  (update-agg! agg (constantly state)))

;; (defn update-aggs! [aggs f & args]
;;   (let [states (mapv deref aggs)
;;         new-states (vec (apply f states args))]
;;     (doseq [[agg state] (zipmap aggs new-states)]
;;       (reset-agg! agg state))
;;     new-states))
