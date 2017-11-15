(ns publicator.impl.storage-test
  (:require
   [clojure.test :as t]
   [publicator.impl.storage :as sut]
   [publicator.impl.test-db :as test-db]
   [publicator.factories :as factories]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.id-generator :as fakes.id-generator]
   [publicator.fakes.hasher :as fakes.hasher]))

(defn- setup [t]
  (with-bindings (merge
                  (fakes.hasher/binding-map)
                  (fakes.id-generator/binging-map)
                  (sut/binding-map test-db/data-source))
    (t)))

(t/use-fixtures :each
  test-db/clear-fixture
  setup)

(t/deftest user
  (t/testing "create"
    (let [user (factories/create-user)]
      (t/is (some? user))
      (t/is (some? (storage/tx-get-one (:id user))))))
  (t/testing "update"
    (let [user      (factories/create-user)
          full-name "new full-name"
          _         (storage/tx-swap! (:id user) assoc :full-name full-name)
          user      (storage/tx-get-one (:id user))]
      (t/is (= full-name (:full-name user)))))
  (t/testing "destroy"
    (let [user (factories/create-user)
          _    (storage/tx-destroy!  (:id user))
          user (storage/tx-get-one (:id user))]
      (t/is (nil? user)))))

(t/deftest special
  (t/testing "create and destroy"
    (let [user-id (storage/with-tx t
                    (let [user (storage/create t (factories/build-user))]
                      (storage/destroy! user)
                      (storage/id user)))]
      (t/is (nil? (storage/tx-get-one user-id))))))

(t/deftest parallel
  (let [user (factories/create-user :posts-count 0)
        id   (:id user)
        n    5
        _    (->> (range n)
                  (map (fn [_] (future (storage/tx-swap! id update :posts-count inc))))
                  (map deref)
                  (doall))
        user (storage/tx-get-one id)]
    (t/is (= n (:posts-count user)))))
