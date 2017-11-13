(ns publicator.impl.storage-test
  (:require
   [clojure.test :as t]

   [jdbc.core :as jdbc]


   [publicator.impl.storage :as sut]
   [publicator.impl.test-data-source :refer [data-source]]
   [publicator.factories :as factories]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.impl.id-generator :as impl.id-generator]
   [publicator.fakes.hasher :as fakes.hasher]))

(t/use-fixtures :each
  (fn [t]
    (with-open [conn (jdbc/connection data-source)]
      (jdbc/atomic
       conn
       (let [with-conn (fn [f] (f conn))]
         (with-bindings (merge
                         (fakes.hasher/binding-map)
                         (impl.id-generator/binding-map with-conn)
                         (sut/binding-map with-conn))
           (t)
           (jdbc/set-rollback! conn)))))))

(t/deftest user
  (t/testing "create"
    (let [user (factories/create-user)]
      (t/is (some? user))
      (t/is (some? (storage/tx-get-one (:id user))))))
  (t/testing "update"
    (let [user      (factories/create-user)
          full-name "new full-name"
          _         (storage/with-tx t
                      (let [user (storage/get-one t (:id user))]
                        (storage/swap! user assoc :full-name full-name)))
          user      (storage/tx-get-one (:id user))]
      (t/is (= full-name (:full-name user)))))
  (t/testing "destroy"
    (let [user (factories/create-user)
          _    (storage/with-tx t
                 (let [user (storage/get-one t (:id user))]
                   (storage/destroy! user)))
          user (storage/tx-get-one (:id user))]
      (t/is (nil? user)))))

(t/deftest special
  (t/testing "create and destroy"
    (let [user-id (storage/with-tx t
                    (let [user (storage/create t (factories/build-user))]
                      (storage/destroy! user)
                      (storage/id user)))]
      (t/is (nil? (storage/tx-get-one user-id))))))
