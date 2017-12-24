(ns publicator.impl.storage-test
  (:require
   [clojure.test :as t]
   [hugsql.core :as hugsql]
   [jdbc.core :as jdbc]
   [publicator.domain.aggregate :as aggregate]
   [publicator.impl.storage :as impl.storage]
   [publicator.impl.test-db :as test-db]
   [publicator.interactors.abstractions.storage :as storage]))

(defrecord TestEntity [id counter])

(defmethod aggregate/spec TestEntity [_] any?)

(defn build-test-entity []
  (TestEntity. 1 0))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(hugsql/def-db-fns "publicator/impl/storage_test.sql")

(defn- get-version [row]
  (-> row :version .getValue))

(defn- row->box [row]
  (let [version (get-version row)
        row     (dissoc row :version)
        entity  (map->TestEntity row)]
     (impl.storage/build-box entity entity (:id entity) version)))

(defn- lock-row->map [row]
  (let [id      (:id row)
        version (get-version row)]
    {:id id, :version version}))

(deftype TestEntityManager []
  impl.storage/Manager
  (-lock [_ conn ids]
    (map lock-row->map (test-entity-locks conn {:ids ids})))
  (-select [_ conn ids]
    (map row->box (test-entity-select conn {:ids ids})))
  (-insert [_ conn boxes]
    (test-entity-insert conn {:vals (map #(-> % deref vals) boxes)}))
  (-delete [_ conn ids]
    (test-entity-delete conn {:ids ids})))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defn- setup [t]
  (with-bindings (impl.storage/binding-map
                  test-db/data-source
                  {TestEntity (TestEntityManager.)}
                  {})
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

(t/deftest identity-map-persisted
  (let [id (:id (storage/tx-create (build-test-entity)))]
    (storage/with-tx t
      (let [x (storage/get-one t id)
            y (storage/get-one t id)]
        (t/is (identical? x y))))))

(t/deftest identity-map-in-memory
  (storage/with-tx t
    (let [x (storage/create t (build-test-entity))
          y (storage/get-one t (storage/id x))]
      (t/is (identical? x y)))))

(t/deftest identity-map-swap
  (storage/with-tx t
    (let [x (storage/create t (build-test-entity))
          y (storage/get-one t (storage/id x))
          _ (storage/swap! x update :counter inc)]
      (t/is (= 1 (:counter @x) (:counter @y))))))

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
