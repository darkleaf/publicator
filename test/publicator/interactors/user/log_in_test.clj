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

(t/use-fixtures :each setup fixtures/all)

(t/deftest main
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/create-agg-in fixtures/*storage* (user/build build-params))
        params       (select-keys build-params [:login :password])
        resp         (sut/process (fixtures/ctx) params)]
    (t/testing "success"
      (t/is (= (:type resp)
               ::sut/processed)))
    (t/testing "sign in"
      (t/is (= (:id user)
               (session/user-id fixtures/*session*))))))

(t/deftest wrong-login
  (let [params {:login    "john_doe"
                :password "secret password"}
        resp   (sut/process (fixtures/ctx) params)]
    (t/testing "has error"
      (t/is (= (:type resp)
               ::sut/authentication-failed)))))

(t/deftest wrong-password
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/create-agg-in fixtures/*storage* (user/build build-params))
        params       {:login    (:login build-params)
                      :password "wrong password"}
        resp         (sut/process (fixtures/ctx) params)]
    (t/testing "has error"
      (t/is (= (:type resp)
               ::sut/authentication-failed)))))

(t/deftest already-logged-in
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/create-agg-in fixtures/*storage* (user/build build-params))
        _            (session/log-in! fixtures/*session* (:id user))
        params       (select-keys build-params [:login :password])
        resp         (sut/process (fixtures/ctx) params)]
    (t/testing "has error"
      (t/is (= (:type resp)
               ::sut/already-logged-in)))))

(t/deftest invalid-params
  (let [params {}
        resp   (sut/process (fixtures/ctx) params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/invalid-params))
      (t/is (contains? resp :explain-data)))))
