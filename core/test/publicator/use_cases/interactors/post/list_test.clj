(ns publicator.use-cases.interactors.post.list-test
  (:require
   [publicator.use-cases.interactors.post.list :as sut]
   [publicator.use-cases.test.fakes :as fakes]
   [publicator.utils.test.instrument :as instrument]
   [publicator.use-cases.test.factories :as factories]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest process
  (let [post        (factories/create-post)
        post-id     (:id post)
        user        (factories/create-user {:posts-ids #{post-id}})
        [tag posts] (sut/process)]
    (t/is (= ::sut/processed tag))
    (t/is (not-empty posts))))
