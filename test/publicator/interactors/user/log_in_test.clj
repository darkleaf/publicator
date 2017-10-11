(ns publicator.interactors.user.log-in-test
  (:require
   [publicator.interactors.user.log-in :as sut]
   [publicator.domain.user :as user]

   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.session :as session]
   [publicator.fakes.session :as fakes.session]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fakes.user-q]

   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen])
  (:import [publicator.domain.user User]))

(declare ^:dynamic *db*)
(declare ^:dynamic *session*)
(declare ^:dynamic *storage*)
(declare ^:dynamic *get-by-login*)

(defn setup [f]
  (let [db (fakes.storage/build-db)]
    (binding [*db*           db
              *session*      (fakes.session/build)
              *storage*      (fakes.storage/build-storage db)
              *get-by-login* (fakes.user-q/build-get-by-login db)]
      (f))))

(t/use-fixtures :each setup fixtures/all)

(defn- ctx []
  {::storage/storage     *storage*
   ::session/session     *session*
   ::user-q/get-by-login *get-by-login*})

(t/deftest main
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/create-agg-in *storage* (user/build build-params))
        params       (select-keys build-params [:login :password])
        [_ err]      (sut/call (ctx) params)]
    (t/testing "no error"
      (t/is (nil? err)))
    (t/testing "sign in"
      (t/is (= (:id user)
               (session/user-id *session*))))))

(t/deftest wrong-login
  (let [params  {:login    "john_doe"
                 :password "secret password"}
        [_ err] (sut/call (ctx) params)]
    (t/testing "no error"
      (t/is (= :wrong-login-or-password
               (:type err))))))

(t/deftest wrong-password
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/create-agg-in *storage* (user/build build-params))
        params       {:login    (:login build-params)
                      :password "wrong password"}
        [_ err]      (sut/call (ctx) params)]
    (t/testing "no error"
      (t/is (= :wrong-login-or-password
               (:type err))))))

(t/deftest already-logged-in
  (let [build-params (sgen/generate (s/gen ::user/build-params))
        user         (storage/create-agg-in *storage* (user/build build-params))
        _            (session/log-in! *session* (:id user))
        params       (select-keys build-params [:login :password])
        [_ err]      (sut/call (ctx) params)]
    (t/testing "error"
      (t/is (= :already-logged-in (:type err))))))

(t/deftest invalid-params
  (let [params  {}
        [_ err] (sut/call (ctx) params)]
    (t/testing "error"
      (t/is (= :invalid-params (:type err)))
      (t/is (contains? err :explain-data)))))
