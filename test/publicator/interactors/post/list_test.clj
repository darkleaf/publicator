(ns publicator.interactors.post.list-test
  (:require
   [publicator.interactors.post.list :as sut]
   [publicator.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]))

(t/use-fixtures :each fixtures/fake-bindings)

(t/deftest process
  (let [post  (factories/create-post)
        resp  (sut/process)
        posts (:posts resp)]
    (t/testing "processed"
      (t/is (= ::sut/processed (:type resp))))
    (t/testing "list"
      (t/is (= 1 (count posts)))
      (t/is (= (:id post) (-> posts first :id))))))
