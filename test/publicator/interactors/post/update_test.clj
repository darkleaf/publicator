(ns publicator.interactors.post.update-test
  (:require
   [publicator.interactors.post.update :as sut]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.domain.post :as post]
   [publicator.domain.user :as user]
   [publicator.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest process
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post :author-id (:id user))
        params (sgen/generate (s/gen ::sut/params))
        resp   (sut/process (:id post)  params)
        post   (:post resp)]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed))
      (t/is (some? post)))
    (t/testing "updated"
      (t/is (= params (select-keys post (keys params)))))))

(t/deftest logged-out
  (let [post   (factories/create-post)
        params (sgen/generate (s/gen ::sut/params))
        resp   (sut/process (:id post) params)]
    (t/testing "has error"
      (t/is (=  (:type resp)
                ::sut/logged-out)))))

(t/deftest another-author
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post)
        params (sgen/generate (s/gen ::sut/params))
        resp   (sut/process (:id post) params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/not-authorized)))))

(t/deftest invalid-params
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post :author-id (:id user))
        params {}
        resp   (sut/process (:id post) params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/invalid-params))
      (t/is (contains? resp  :explain-data)))))

(t/deftest initial-params
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post :author-id (:id user))
        resp   (sut/initial-params (:id post))]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/initial-params))
      (t/is (contains? resp  :initial-params)))))
