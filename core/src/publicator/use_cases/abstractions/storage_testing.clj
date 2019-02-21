(ns publicator.use-cases.abstractions.storage-testing
  (:require
   [publicator.use-cases.abstractions.storage :as sut]
   [publicator.domain.aggregate :as aggregate]
   [clojure.test :as t]
   [datascript.core :as d]))

;; проверка на смену идентификатора и типа

(def ^:private ^:const id 42)

(defn- build-agg []
  (aggregate/build ::aggregate id [[:db/add :root :counter 0]]))

(defn test-create-&-get []
  (let [agg (build-agg)]
    (sut/just-create agg)
    (t/is (= agg (sut/just-get-one ::aggregate id)))))

(defn test-alter []
  (let [agg     (build-agg)
        tx-data [[:db/add :root :counter 1]]
        agg'    (d/db-with agg tx-data)
        _       (sut/just-create agg)
        _       (sut/just-alter ::aggregate id d/db-with tx-data)
        agg''   (sut/just-get-one ::aggregate id)]
    (t/is (= agg' agg''))))

(defn test-identity-map-persisted []
  (let [agg (sut/just-create (build-agg))]
    (sut/atomic t
      (let [iagg  (sut/get-one t ::aggregate id)
            iagg' (sut/get-one t ::aggregate id)]
        (t/is (identical? iagg iagg'))))))

(defn test-identity-map-in-memory []
  (sut/atomic t
    (let [iagg  (sut/create t (build-agg))
          iagg' (sut/get-one t ::aggregate id)]
      (t/is (identical? iagg iagg')))))

(defn test-identity-map-swap []
  (sut/atomic t
    (let [iagg    (sut/create t (build-agg))
          iagg'   (sut/get-one t ::aggregate id)]
      (dosync (alter iagg d/db-with [[:db/add :root :counter 1]]))
      (t/is (= @iagg @iagg')))))
