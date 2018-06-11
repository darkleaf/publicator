(ns publicator.use-cases.interactors.post.show-test
  (:require
   [publicator.use-cases.interactors.post.show :as sut]
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
  (let [user (factories/create-user)
        post (factories/create-post)
        _    (storage/tx-alter user user-posts/add-post post)]
    (t/testing "guest"
      (let [[tag post] (sut/process (:id post))]
        (t/is (= ::sut/processed tag))
        (t/is (some? post))))
    (t/testing "user"
      (let [_          (user-session/log-in! user)
            [tag post] (sut/process (:id post))]
        (t/is (= ::sut/processed tag))
        (t/is (some? post))
        (t/is (-> post ::sut/can-edit? true?))))))
