(ns publicator.impl.storage-test
  (:require
   [clojure.test :as t]
   [hugsql.core :as hugsql]
   [jdbc.core :as jdbc]
   [publicator.impl.storage :as sut]
   [publicator.impl.test-db :as test-db]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [publicator.interactors.abstractions.storage :as storage]))

(hugsql/def-db-fns "publicator/impl/storage_test.sql")

(defn- setup [t]
  (with-bindings (sut/binding-map test-db/data-source)
    (t)))

(defn- table [t]
  (with-open [conn (jdbc/connection test-db/data-source)]
    (try
      (create-test-entity-table conn)
      (t)
      (finally
        (drop-test-entity-table conn)))))

(t/use-fixtures :each
  test-db/clear-fixture
  setup
  table)

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defrecord TestEntity [id counter]
  aggregate/Aggregate
  (spec [_] any?))

(defn build-test-entity []
  (TestEntity. 1 0))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defmethod sut/insert-for TestEntity [_ conn boxes]
  (test-entity-insert conn {:vals (map #(-> % deref vals) boxes)}))

(defn- get-version [row]
  (-> row :version .getValue))

(defn- row->box [row]
  (let [version (get-version row)
        row     (dissoc row :version)
        entity  (map->TestEntity row)]
     (sut/build-box entity entity (:id entity) version)))

(defmethod sut/select-for TestEntity [_ conn ids]
  (map row->box (test-entity-select conn {:ids ids})))

(defmethod sut/delete-for TestEntity [_ conn ids]
  (test-entity-delete conn {:ids ids}))

(defn- lock-row->pair [row]
  (let [id      (:id row)
        version (get-version row)]
    [id version]))

(defmethod sut/locks-for TestEntity [_ conn ids]
  (into {} (map lock-row->pair (test-entity-locks conn {:ids ids}))))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(t/deftest create
  (let [entity (storage/tx-create (build-test-entity))]
    (t/is (some? entity))
    (t/is (some? (storage/tx-get-one (:id entity))))))

(t/deftest swap
  (let [entity (storage/tx-create (build-test-entity))
        _      (storage/tx-swap! (:id entity) update :counter inc)
        entity (storage/tx-get-one (:id entity))]
    (t/is (= 1 (:counter entity)))))

(t/deftest destroy
  (let [entity (storage/tx-create (build-test-entity))
        _      (storage/tx-destroy! (:id entity))
        entity (storage/tx-get-one (:id entity))]
      (t/is (nil? entity))))

(t/deftest nop
  (let [id (storage/with-tx t
             (let [entity (storage/create t (build-test-entity))]
               (storage/destroy! entity)
               (storage/id entity)))]
    (t/is (nil? (storage/tx-get-one id)))))

(t/deftest locks
  (let [entity (storage/tx-create (build-test-entity))
        id     (:id entity)
        n      5
        _      (->> (range n)
                    (map (fn [_] (future (storage/tx-swap! id update :counter inc))))
                    (map deref)
                    (doall))
        entity (storage/tx-get-one id)]
    (t/is (= n (:counter entity)))))


;; (t/deftest user
;;   (t/testing "create"
;;     (let [user (factories/create-user)]
;;       (t/is (some? user))
;;       (t/is (some? (storage/tx-get-one (:id user))))))
;;   (t/testing "update"
;;     (let [user      (factories/create-user)
;;           full-name "new full-name"
;;           _         (storage/tx-swap! (:id user) assoc :full-name full-name)
;;           user      (storage/tx-get-one (:id user))]
;;       (t/is (= full-name (:full-name user)))))
;;   (t/testing "destroy"
;;     (let [user (factories/create-user)
;;           _    (storage/tx-destroy!  (:id user))
;;           user (storage/tx-get-one (:id user))]
;;       (t/is (nil? user)))))

;; (t/deftest special
;;   (t/testing "create and destroy"
;;     (let [user-id (storage/with-tx t
;;                     (let [user (storage/create t (factories/build-user))]
;;                       (storage/destroy! user)
;;                       (storage/id user)))]
;;       (t/is (nil? (storage/tx-get-one user-id))))))
