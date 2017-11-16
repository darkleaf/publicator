(ns publicator.impl.storage.post-manager-test
  (:require
   [clojure.test :as t]
   [publicator.factories :as factories]
   [publicator.fake.hasher :as fake.hasher]
   [publicator.fake.id-generator :as fake.id-generator]
   [publicator.impl.storage :as impl.storage]
   [publicator.impl.storage.post-manager :as sut]
   [publicator.impl.storage.user-manager :as user-manager]
   [publicator.impl.test-db :as test-db]
   [publicator.interactors.abstractions.storage :as storage]))

(defn- setup [t]
  (with-bindings (merge
                  (fake.hasher/binding-map)
                  (fake.id-generator/binging-map)
                  (impl.storage/binding-map test-db/data-source
                                            (merge
                                             (sut/manager)
                                             (user-manager/manager))))
    (t)))

(t/use-fixtures :each
  test-db/clear-fixture
  setup)

(t/deftest create
  (let [entity (factories/create-post)]
    (t/is (some? entity))
    (t/is (some? (storage/tx-get-one (:id entity))))))

(t/deftest swap
  (let [entity (factories/create-post)
        title  "new title"
        _      (storage/tx-swap! (:id entity) assoc :title title)
        entity (storage/tx-get-one (:id entity))]
    (t/is (= title (:title entity)))))

(t/deftest destroy
  (let [entity (factories/create-post)
        _      (storage/tx-destroy! (:id entity))
        entity (storage/tx-get-one (:id entity))]
      (t/is (nil? entity))))
