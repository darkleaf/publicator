(ns publicator.interactors.post.show-test
  (:require
   [publicator.interactors.post.show :as sut]
   [clojure.test :as t]
   [publicator.factories :as factories]
   [publicator.interactors.fixtures :as fixtures]))

(t/use-fixtures :each fixtures/all)

(t/deftest process
  (let [post (factories/create-post)
        resp (sut/process (:id post))]
    (t/testing "success"
      (t/is (= ::sut/processed (:type resp))))
    (t/testing "some post"
      (t/is (some? (:post resp))))))

(t/deftest not-found
  (let [resp (sut/process 1)]
    (t/testing "not found"
      (t/is (= ::sut/not-found (:type resp))))))
