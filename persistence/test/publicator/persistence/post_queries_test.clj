(ns publicator.persistence.post-queries-test
  (:require
   [clojure.test :as t]
   [publicator.utils.test.instrument :as instument]
   [publicator.use-cases.test.factories :as factories]
   [publicator.domain.test.fakes.password-hasher :as fakes.password-hasher]
   [publicator.domain.test.fakes.id-generator :as fakes.id-generator]
   [publicator.persistence.storage :as persistence.storage]
   [publicator.persistence.storage.user-mapper :as user-mapper]
   [publicator.persistence.storage.post-mapper :as post-mapper]
   [publicator.persistence.test.db :as db]
   [publicator.use-cases.abstractions.post-queries :as post-q]
   [publicator.persistence.post-queries :as sut]
   [publicator.domain.aggregates.user :as user]))

(defn setup [t]
  (with-bindings (merge
                  (fakes.password-hasher/binding-map)
                  (fakes.id-generator/binding-map)
                  (persistence.storage/binding-map db/*data-source*
                                                   (merge
                                                    (user-mapper/mapper)
                                                    (post-mapper/mapper)))
                  (sut/binding-map db/*data-source*))
    (t)))

(t/use-fixtures :once
  instument/fixture
  db/once-fixture)

(t/use-fixtures :each
  db/each-fixture
  setup)

(defn post-with-user [post user]
  (assoc post
         ::user/id (:id user)
         ::user/full-name (:full-name user)))

(t/deftest get-list-found
  (let [post (factories/create-post)
        user (factories/create-user {:posts-ids [(:id post)]})
        res  (post-q/get-list)
        item (first res)]
    (t/is (= 1 (count res)))
    (t/is (= (post-with-user post user)
             item))))

(t/deftest get-list-empty
  (let [res (post-q/get-list)]
    (t/is (empty? res))))

(t/deftest get-by-id
  (let [post (factories/create-post)
        id   (:id post)
        user (factories/create-user {:posts-ids [id]})
        item (post-q/get-by-id id)]
    (t/is (= (post-with-user post user)
             item))))
