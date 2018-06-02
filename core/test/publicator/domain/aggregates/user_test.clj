(ns publicator.domain.aggregates.user-test
  (:require
   [publicator.domain.aggregates.user :as sut]
   [publicator.domain.test.fakes :as fakes]
   [publicator.utils.test.instrument :as instrument]
   [publicator.domain.test.factories :as factories]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest build
  (let [params {:login     "john_doe"
                :full-name "John Doe"
                :password  "password"}
        user   (sut/build params)]
    (t/is (sut/user? user))))

(t/deftest authenticated?
  (let [password (factories/gen ::sut/password)
        user (factories/build-user {:password password})]
    (t/is (sut/authenticated? user password))))

(t/deftest aggregate
  (t/testing "id"
    (let [user (factories/build-user)]
      (t/is (= (:id user)
               (aggregate/id user)))))
  (t/testing "spec"
    (let [user (factories/build-user)
          spec (aggregate/spec user)]
      (t/is (s/valid? spec user)))))
