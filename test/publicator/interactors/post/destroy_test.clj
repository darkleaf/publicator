(ns publicator.interactors.post.destroy-test
  (:require
   [publicator.interactors.post.destroy :as sut]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.domain.post :as post]
   [publicator.domain.user :as user]
   [publicator.interactors.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]))

(t/use-fixtures :each fixtures/all)

(t/deftest process
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post :author-id (:id user))
        resp   (sut/process (:id post))]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed)))
    (t/testing "destroyed"
      (t/is (nil? (storage/tx-get-one (:id post)))))))

(t/deftest logged-out
  (let [post   (factories/create-post)
        resp   (sut/process (:id post))]
    (t/testing "has error"
      (t/is (=  (:type resp) ::sut/logged-out)))))

(t/deftest another-author
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post)
        resp   (sut/process (:id post))]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/not-authorized)))))
