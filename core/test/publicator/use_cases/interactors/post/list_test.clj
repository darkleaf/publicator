(ns publicator.use-cases.interactors.post.list-test
  (:require
   [publicator.use-cases.interactors.post.list :as sut]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.domain.services.user-posts :as user-posts]
   [publicator.use-cases.test.fakes :as fakes]
   [publicator.utils.test.instrument :as instrument]
   [publicator.use-cases.test.factories :as factories]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest process
  (let [user         (factories/create-user)
        post         (factories/create-post)
        another-post (factories/create-post)
        _            (storage/tx-alter user user-posts/add-post post)]
    (t/testing "guest"
      (let [[tag posts] (sut/process)]
        (t/testing "success"
          (t/is (= ::sut/processed tag))
          (t/is (not-empty posts)))
        (t/testing "can not edit"
          (t/is (every? #(-> % ::sut/can-edit? false?) posts)))))
    (t/testing "logged in"
      (let [_           (user-session/log-in! user)
            [tag posts] (sut/process)]
        (t/testing "success"
          (t/is (= ::sut/processed tag))
          (t/is (not-empty posts)))
        (t/testing "can edit"
          (t/is (some #(-> % ::sut/can-edit? true?) posts)))))))
