(ns publicator.impl.user-queries-test
  (:require
   [clojure.test :as t]
   [publicator.factories :as factories]
   [publicator.fake.hasher :as fake.hasher]
   [publicator.fake.id-generator :as fake.id-generator]
   [publicator.impl.storage :as impl.storage]
   [publicator.impl.storage.user-manager :as user-manager]
   [publicator.impl.test-db :as test-db]
   [publicator.impl.user-queries :as impl.user-q]
   [publicator.interactors.abstractions.user-queries :as user-q]))

(defn set-bindings [t]
  (with-bindings (merge
                  (fake.hasher/binding-map)
                  (fake.id-generator/binding-map)
                  (impl.storage/binding-map test-db/data-source
                                            (user-manager/manager))
                  (impl.user-q/binding-map test-db/data-source))
    (t)))

(t/use-fixtures :each set-bindings test-db/clear-fixture)

(t/deftest get-by-login
  (t/testing "found"
    (let [created-user (factories/create-user)
          found-user   (user-q/get-by-login (:login created-user))]
      (t/is (= created-user found-user))))
  (t/testing "not-found"
    (let [found-user (user-q/get-by-login "wrong login")]
      (t/is (nil? found-user)))))
