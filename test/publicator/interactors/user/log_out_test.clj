(ns publicator.interactors.user.log-out-test
  (:require
   [publicator.interactors.user.log-out :as sut]
   [publicator.domain.user :as user]
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]))

(t/use-fixtures :each fixtures/all)

(t/deftest main
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/tx-create (user/build build-params))
        _            (session/log-in! (:id user))
        resp         (sut/process)]
    (t/testing "success"
      (t/is (= (:type resp) ::sut/processed)))
    (t/testing "logged out"
      (t/is (session/logged-out?)))))

(t/deftest already-logged-out
  (let [resp (sut/process)]
    (t/testing "has error"
      (t/is (= (:type resp) ::sut/already-logged-out)))))
