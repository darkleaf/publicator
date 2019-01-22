(ns publicator-ext.domain.aggregates.user-test
  (:require
   [publicator-ext.domain.aggregates.user :as sut]
   [publicator-ext.domain.test.fakes :as fakes]
   [publicator-ext.utils.test.instrument :as instrument]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.abstractions.instant :as instant]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(t/use-fixtures :each fakes/fixture)
(t/use-fixtures :once instrument/fixture)

(t/deftest build
  (let [params {:user/login    "john"
                :user/password "12345678"}
        user   (sut/build params)]
    (t/is (some? user))))

;; (t/deftest authenticated?
;;   (let [password (factories/gen ::sut/password)
;;         user (factories/build-user {:password password})]
;;     (t/is (sut/authenticated? user password))))
