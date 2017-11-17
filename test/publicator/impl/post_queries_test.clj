(ns publicator.impl.post-queries-test
  (:require
   [clojure.test :as t]
   [publicator.factories :as factories]
   [publicator.fake.hasher :as fake.hasher]
   [publicator.fake.id-generator :as fake.id-generator]
   [publicator.impl.storage :as impl.storage]
   [publicator.impl.storage.user-manager :as user-manager]
   [publicator.impl.storage.post-manager :as post-manager]
   [publicator.impl.test-db :as test-db]
   [publicator.impl.post-queries :as impl.post-q]
   [publicator.interactors.abstractions.post-queries :as post-q]))

(defn set-bindings [t]
  (with-bindings (merge
                  (fake.hasher/binding-map)
                  (fake.id-generator/binding-map)
                  (impl.storage/binding-map test-db/data-source
                                            (merge
                                             (user-manager/manager)
                                             (post-manager/manager)))
                  (impl.post-q/binding-map test-db/data-source))
    (t)))

(t/use-fixtures :each set-bindings test-db/clear-fixture)

(t/deftest get-list-found
  (let [post (factories/create-post)
        res  (post-q/get-list)
        item (first res)]
    (t/is (= 1 (count res)))
    (t/is (= (:id post) (:id item)))))

(t/deftest get-list-empty
  (let [res (post-q/get-list)]
    (t/is (empty? res))))
