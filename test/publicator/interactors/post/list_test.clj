(ns publicator.interactors.post.list-test
  (:require
   [publicator.interactors.post.list :as sut]
   [publicator.interactors.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]))

(t/use-fixtures :each fixtures/all)

(t/deftest process
  (let [post (factories/create-post)
        resp (sut/process)]
    (t/testing "processed"
      (t/is (= ::sut/processed (:type resp))))
    (t/testing "list"
      (t/is (= [post] (:posts resp))))))
