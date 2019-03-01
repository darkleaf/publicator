(ns publicator.use-cases.abstractions.storage-testing
  (:require
   [publicator.use-cases.abstractions.storage :as sut]
   [publicator.domain.aggregate :as aggregate]
   [clojure.test :as t]
   [datascript.core :as d]))

;; проверка на смену идентификатора и типа

(def ^:private ^:const id 42)

(defn- just-create [state]
  (sut/transaction
    (sut/*create* state))
  nil)

(defn- just-get [type id]
  (sut/transaction
   (when-let [x (sut/*get* type id)]
     @x)))

(defn- just-alter [type id])

(defn- just-alter [type id f & args]
  (sut/transaction
   (when-let [x (sut/*get* type id)]
     (dosync
      (apply alter x f args)))))

(defn- build-agg []
  (aggregate/build ::aggregate id [[:db/add :root :counter 0]]))

(defn test-create-&-get []
  (let [agg (build-agg)]
    (just-create agg)
    (t/is (= agg (just-get ::aggregate id)))))

(defn test-alter []
  (let [agg     (build-agg)
        tx-data [[:db/add :root :counter 1]]
        agg'    (d/db-with agg tx-data)
        _       (just-create agg)
        _       (just-alter ::aggregate id d/db-with tx-data)
        agg''   (just-get ::aggregate id)]
    (t/is (= agg' agg''))))

(defn test-identity-map-persisted []
  (let [agg (sut/just-create (build-agg))]
    (sut/transaction
     (let [iagg  (sut/*get* ::aggregate id)
           iagg' (sut/*get* ::aggregate id)]
       (t/is (identical? iagg iagg'))))))

(defn test-identity-map-in-memory []
  (sut/transaction
   (let [iagg  (sut/*create* (build-agg))
         iagg' (sut/*get* ::aggregate id)]
     (t/is (identical? iagg iagg')))))

(defn test-identity-map-swap []
  (sut/transaction
   (let [iagg  (sut/*create* (build-agg))
         iagg' (sut/*get* ::aggregate id)]
     (dosync (alter iagg d/db-with [[:db/add :root :counter 1]]))
     (t/is (= @iagg @iagg')))))
