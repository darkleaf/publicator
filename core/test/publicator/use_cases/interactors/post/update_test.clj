(ns publicator.use-cases.interactors.post.update-test
  (:require
   [publicator.use-cases.interactors.post.update :as sut]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.use-cases.test.fakes :as fakes]
   [publicator.utils.test.instrument :as instrument]
   [publicator.use-cases.test.factories :as factories]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest process
  (let [post       (factories/create-post)
        post-id    (:id post)
        user       (factories/create-user {:posts-ids #{post-id}})
        _          (user-session/log-in! user)
        params     (factories/gen ::sut/params)
        [tag post] (sut/process post-id  params)]
    (t/testing "success"
      (t/is (= ::sut/processed tag)))
    (t/testing "updated"
      (t/is (= params (select-keys post (keys params)))))))

(t/deftest logged-out
  (let [post   (factories/create-post)
        params (factories/gen ::sut/params)
        [tag]  (sut/process (:id post) params)]
    (t/testing "has error"
      (t/is (=  ::sut/logged-out tag)))))

(t/deftest another-author
  (let [user   (factories/create-user)
        _      (user-session/log-in! user)
        post   (factories/create-post)
        params (factories/gen ::sut/params)
        [tag]  (sut/process (:id post) params)]
    (t/testing "error"
      (t/is (= ::sut/not-authorized tag)))))

(t/deftest invalid-params
  (let [post    (factories/create-post)
        post-id (:id post)
        user    (factories/create-user {:posts-ids #{post-id}})
        _       (user-session/log-in! user)
        params  {}
        [tag]   (sut/process post-id params)]
    (t/testing "error"
      (t/is (= ::sut/invalid-params tag)))))

(t/deftest not-found
  (let [wrong-id 42
        user     (factories/create-user {:posts-ids #{wrong-id}})
        _        (user-session/log-in! user)
        params   (factories/gen ::sut/params)
        [tag]    (sut/process wrong-id params)]
    (t/testing "error"
      (t/is (= ::sut/not-found tag)))))

(t/deftest initial-params
  (let [post    (factories/create-post)
        post-id (:id post)
        user    (factories/create-user {:posts-ids #{post-id}})
        _       (user-session/log-in! user)
        [tag]   (sut/initial-params (:id post))]
    (t/testing "success"
      (t/is (=  ::sut/initial-params tag)))))
