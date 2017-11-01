(ns publicator.interactors.user.log-in-test
  (:require
   [publicator.interactors.user.log-in :as sut]
   [publicator.domain.user :as user]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest main
  (let [password (sgen/generate (s/gen ::user/password))
        user     (factories/create-user :password password)
        params   {:login    (:login user)
                  :password password}
        resp     (sut/process params)]
    (t/testing "success"
      (t/is (= (:type resp)  ::sut/processed)))
    (t/testing "sign in"
      (t/is (= user (user-session/user))))))

(t/deftest wrong-login
  (let [params {:login    "john_doe"
                :password "secret password"}
        resp   (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/authentication-failed)))))

(t/deftest wrong-password
  (let [user         (factories/create-user)
        params       {:login    (:login user)
                      :password "wrong password"}
        resp         (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/authentication-failed)))))

(t/deftest already-logged-in
  (let [user         (factories/create-user)
        _            (user-session/log-in! user)
        params       {:login "foo"
                      :password "bar"}
        resp         (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/already-logged-in)))))

(t/deftest invalid-params
  (let [params {}
        resp   (sut/process params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/invalid-params))
      (t/is (contains? resp :explain-data)))))
