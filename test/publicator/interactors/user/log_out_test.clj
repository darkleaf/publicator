(ns publicator.interactors.user.log-out-test
  (:require
   [publicator.interactors.user.log-out :as sut]
   [publicator.domain.user :as user]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest main
  (let [user (factories/create-user)
        _    (user-session/log-in! user)
        resp (sut/process)]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed)))
    (t/testing "logged out"
      (t/is (user-session/logged-out?)))))

(t/deftest already-logged-out
  (let [resp (sut/process)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/already-logged-out)))))
