(ns publicator.interactors.user.log-in-test
  (:require
   [publicator.interactors.user.log-in :as sut]
   [publicator.domain.user :as user]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen])
  (:import [publicator.domain.user User]))

(t/use-fixtures :each fixtures/all)

(t/deftest main
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/tx-create (user/build build-params))
        params       (select-keys build-params [:login :password])
        resp         (sut/process params)]
    (t/testing "success"
      (t/is (= (:type resp)  ::sut/processed)))
    (t/testing "sign in"
      (t/is (= (:id user) (session/user-id))))))

(t/deftest wrong-login
  (let [params {:login    "john_doe"
                :password "secret password"}
        resp   (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/authentication-failed)))))

(t/deftest wrong-password
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/tx-create (user/build build-params))
        params       {:login    (:login build-params)
                      :password "wrong password"}
        resp         (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/authentication-failed)))))

(t/deftest already-logged-in
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/tx-create (user/build build-params))
        _            (session/log-in! (:id user))
        params       (select-keys build-params [:login :password])
        resp         (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/already-logged-in)))))

(t/deftest invalid-params
  (let [params {}
        resp   (sut/process params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/invalid-params))
      (t/is (contains? resp :explain-data)))))
