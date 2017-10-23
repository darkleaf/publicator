(ns publicator.interactors.user.register-test
  (:require
   [publicator.interactors.user.register :as sut]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.domain.user :as user]
   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest process
  (let [params  (sgen/generate (s/gen ::user/build-params))
        resp    (sut/process params)
        user-id (-> resp :user :id)]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed)))
    (t/testing "sign in"
      (t/is (= user-id (session/user-id))))
    (t/testing "persisted"
      (let [user (storage/tx-get-one user-id)]
        (t/is (= (:login params) (:login user)))))))

(t/deftest already-registered
  (let [params (sgen/generate (s/gen ::user/build-params))
        _      (storage/tx-create (user/build params))
        resp   (sut/process params)]
    (t/testing "has error"
      (t/is (= (:type resp)
               ::sut/already-registered)))
    (t/testing "not sign in"
      (t/is (session/logged-out?)))))

(t/deftest already-logged-in
  (let [params  (sgen/generate (s/gen ::user/build-params))
        user    (storage/tx-create (user/build params))
        user-id (:id user)
        _       (session/log-in! user-id)
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
