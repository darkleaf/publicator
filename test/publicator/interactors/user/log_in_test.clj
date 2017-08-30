(ns publicator.interactors.user.log-in-test
  (:require
   [publicator.interactors.user.log-in :as sut]
   [publicator.interactors.helpers.user-session :as user-session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.session :as fakes.session]
   [publicator.domain.user :as user]
   [publicator.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen])
  (:import [publicator.domain.user User]))

(declare ^:dynamic *session*)
(declare ^:dynamic *tx-factory*)

(defn setup [f]
  (binding [*session* (fakes.session/build)]
    (f)))

;; фикстуры, наверно надо перенести в неймспейс интеракторов
(t/use-fixtures :each setup fixtures/all)

(defn get-by-login-stub [attrs]
  (reify
    user-q/PGetByLogin
    (-get-by-login [_ _login] attrs)))

(t/deftest main
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (user/build build-params)
        ctx          {:session            *session*
                      :get-by-login-query (get-by-login-stub user)}
        params       (select-keys build-params [:login :password])
        [_ err]      (sut/call ctx params)]
    (t/testing "no error"
      (t/is (nil? err)))
    (t/testing "sign in"
      (t/is (= (:id user)
               (user-session/user-id *session*))))))
