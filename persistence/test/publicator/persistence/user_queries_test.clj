(ns publicator.persistence.user-queries-test
  (:require
   [clojure.test :as t]
   [publicator.utils.test.instrument :as instument]
   [publicator.use-cases.test.factories :as factories]
   [publicator.domain.test.fakes.password-hasher :as fakes.password-hasher]
   [publicator.domain.test.fakes.id-generator :as fakes.id-generator]
   [publicator.persistence.storage :as persistence.storage]
   [publicator.persistence.storage.user-mapper :as user-mapper]
   [publicator.persistence.test.db :as db]
   [publicator.use-cases.abstractions.user-queries :as user-q]
   [publicator.persistence.user-queries :as sut]))

(defn setup [t]
  (with-bindings (merge
                  (fakes.password-hasher/binding-map)
                  (fakes.id-generator/binding-map)
                  (persistence.storage/binding-map db/*data-source*
                                                   (user-mapper/mapper))
                  (sut/binding-map db/*data-source*))
    (t)))

(t/use-fixtures :once
  instument/fixture
  db/once-fixture)

(t/use-fixtures :each
  db/each-fixture
  setup)

(t/deftest get-found
  (let [user (factories/create-user)
        item  (user-q/get-by-login (:login user))]
    (t/is (= user item))))

(t/deftest get-not-found
  (let [item  (user-q/get-by-login "some_login")]
    (t/is (nil? item))))
