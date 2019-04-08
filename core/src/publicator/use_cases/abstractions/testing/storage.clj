(ns publicator.use-cases.abstractions.testing.storage
  (:require
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.aggregate :as agg]
   [publicator.utils.datascript.validation :as d.validation]
   [clojure.test :as t]))

;; проверка на смену идентификатора и типа

(def ^:private ^:const id 42)

(defn- just-create [state]
  (storage/transaction
    (storage/*create* state))
  nil)

(defn- just-get [type id]
  (storage/transaction
   (when-let [x (storage/*get* type id)]
     @x)))

(defn- just-alter [type id])

(defn- just-alter [type id f & args]
  (storage/transaction
   (when-let [x (storage/*get* type id)]
     (dosync
      (apply alter x f args)))))

(def ^:private spec
  {:type         ::test-agg
   :id-generator (constantly id)
   :validator (d.validation/predicate [[:counter pos-int?]])})

(defn- build-agg []
  (-> (agg/build spec)
      (agg/change [[:db/add :root :counter 0]]
                  agg/allow-everething)))

(defn test-create-&-get []
  (let [agg (build-agg)]
    (just-create agg)
    (t/is (= agg (just-get ::test-agg id)))))

(defn test-alter []
  (let [agg     (build-agg)
        tx-data [[:db/add :root :counter 1]]
        agg'    (agg/change agg tx-data agg/allow-everething)
        _       (just-create agg)
        _       (just-alter ::test-agg id agg/change tx-data agg/allow-everething)
        agg''   (just-get ::test-agg id)]
    (t/is (= agg' agg''))))

(defn test-identity-map-persisted []
  (let [agg (just-create (build-agg))]
    (storage/transaction
     (let [iagg  (storage/*get* ::test-agg id)
           iagg' (storage/*get* ::test-agg id)]
       (t/is (identical? iagg iagg'))))))

(defn test-identity-map-in-memory []
  (storage/transaction
   (let [iagg  (storage/*create* (build-agg))
         iagg' (storage/*get* ::test-agg id)]
     (t/is (identical? iagg iagg')))))

(defn test-identity-map-swap []
  (storage/transaction
   (let [iagg  (storage/*create* (build-agg))
         iagg' (storage/*get* ::test-agg id)]
     (dosync (alter iagg agg/change [[:db/add :root :counter 1]] agg/allow-everething))
     (t/is (= @iagg @iagg')))))
