(ns publicator.interactors.user.log-in-test
  (:require
   [publicator.interactors.user.log-in :as sut]
   [publicator.domain.user :as user]
   [publicator.interactors.abstractions.session :as session]
   [publicator.fakes.session :as fakes.session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.stubs.user-queries :as stubs.user-q]
   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen])
  (:import [publicator.domain.user User]))

(declare ^:dynamic *session*)

(defn setup [f]
  (binding [*session* (fakes.session/build)]
    (f)))

(t/use-fixtures :each setup fixtures/all)

(t/deftest main
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (user/build build-params)
        ctx          {::session/session     *session*
                      ::user-q/get-by-login (stubs.user-q/get-by-login user)}
        params       (select-keys build-params [:login :password])
        [_ err]      (sut/call ctx params)]
    (t/testing "no error"
      (t/is (nil? err)))
    (t/testing "sign in"
      (t/is (= (:id user)
               (session/user-id *session*))))))
