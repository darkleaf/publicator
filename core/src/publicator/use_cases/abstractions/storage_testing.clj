(ns publicator.use-cases.abstractions.storage-testing
  (:require
   [publicator.use-cases.abstractions.storage :as sut]
   [publicator.domain.aggregate :as aggregate]
   [clojure.test :as t]
   [datascript.core :as d]))

;; проверка на смену идентификатора и типа

(def ^:private ^:const id 42)

(defn- build-agg []
  (aggregate/build :test id [[:db/add :root :counter 0]]))

(defn test-create-&-get []
  (let [agg (build-agg)]
    (t/is (= agg
             (sut/atomic t
               @(sut/create t agg))
             (sut/atomic t
               @(sut/get-one t id))))))

(defn test-alter []
  (let [test    (build-agg)
        tx-data [[:db/add :root :counter 1]]
        test'   (d/db-with test tx-data)
        _       (sut/atomic t
                  (sut/create t test))
        _       (sut/atomic t
                  (let [iagg (sut/get-one t id)]
                    (dosync
                     (alter iagg d/db-with tx-data))))
        test''  (sut/atomic t
                  @(sut/get-one t id))]
    (t/is (= test' test''))))

;; (defn test-identity-map-persisted
;;   (let [test (storage/tx-create (->Test 0))
;;         id   (aggregate/id test)]
;;     (storage/with-tx t
;;       (let [x (storage/get-one t id)
;;             y (storage/get-one t id)]
;;         (t/is (identical? x y))))))

;; (t/deftest identity-map-in-memory
;;   (storage/with-tx t
;;     (let [x (storage/create t (->Test 0))
;;           y (storage/get-one t (aggregate/id @x))]
;;       (t/is (identical? x y)))))

;; (t/deftest identity-map-swap
;;   (storage/with-tx t
;;     (let [x (storage/create t (->Test 0))
;;           y (storage/get-one t (aggregate/id @x))
;;           _ (dosync (alter x update :counter inc))]
;;       (t/is (= 1 (:counter @x) (:counter @y))))))










;; (t/deftest concurrency
;;   (let [test (storage/tx-create (->Test 0))
;;         id   (aggregate/id test)
;;         n    10
;;         _    (->> (repeatedly #(future (storage/tx-alter test update :counter inc)))
;;                   (take n)
;;                   (doall)
;;                   (map deref)
;;                   (doall))
;;         test (storage/tx-get-one id)]
;;     (t/is (= n (:counter test)))))

;; (t/deftest inner-concurrency
;;   (let [test (storage/tx-create (->Test 0))
;;         id   (aggregate/id test)
;;         n    10
;;         _    (storage/with-tx t
;;                (->> (repeatedly #(future (as-> id <>
;;                                            (storage/get-one t <>)
;;                                            (dosync (alter <> update :counter inc)))))
;;                     (take n)
;;                     (doall)
;;                     (map deref)
;;                     (doall)))
;;         test (storage/tx-get-one id)]
;;     (t/is (= n (:counter test)))))
