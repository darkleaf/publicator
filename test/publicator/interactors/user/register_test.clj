(ns publicator.interactors.user.register-test
  (:require
   [publicator.interactors.user.register :as sut]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.domain.user :as user]
   [publicator.interactors.fixtures :as fixtures]
   [publicator.factories :as factories]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest process
  (let [params (sgen/generate (s/gen ::sut/params))
        resp   (sut/process params)
        user   (:user resp)]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed)))
    (t/testing "sign in"
      (t/is (= user (user-session/user))))
    (t/testing "persisted"
      (t/is (some? (storage/tx-get-one (:id user)))))))

(t/deftest already-registered
  (let [params (sgen/generate (s/gen ::sut/params))
        _      (factories/create-user :login (:login params))
        resp   (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp)
               ::sut/already-registered)))
    (t/testing "not sign in"
      (t/is (user-session/logged-out?)))))

(t/deftest already-logged-in
  (let [user    (factories/create-user)
        _       (user-session/log-in! user)
        params  (sgen/generate (s/gen ::sut/params))
        resp    (sut/process params)]
    (t/testing "has error"
      (t/is (=  (:type resp)
                ::sut/already-logged-in)))))

(t/deftest invalid-params
  (let [params {}
        resp   (sut/process params)]
    (t/testing "error"
      (t/is (= (:type resp) ::sut/invalid-params))
      (t/is (contains? resp  :explain-data)))))
