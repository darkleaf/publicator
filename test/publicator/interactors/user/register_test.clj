(ns publicator.interactors.user.register-test
  (:require
   [publicator.interactors.user.register :as sut]

   [publicator.interactors.abstractions.storage :as storage]
   [publicator.fakes.storage :as fakes.storage]

   [publicator.interactors.abstractions.session :as session]
   [publicator.fakes.session :as fakes.session]

   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.fakes.user-queries :as fakes.user-q]

   [publicator.domain.user :as user]
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
  (let [params     (sgen/generate (s/gen ::user/build-params))
        [user err] (sut/call (ctx) params)
        user-id    (:id user)]
    (t/testing "no error"
      (t/is (nil? err)))
    (t/testing "sign in"
      (t/is (= user-id
               (session/user-id *session*))))
    (t/testing "persisted"
      (let [user (storage/get-agg-from *storage* user-id)]
        (t/is (= (:login params) (:login @user)))))))

(t/deftest already-registered
  (let [params  (sgen/generate (s/gen ::user/build-params))
        _       (storage/create-agg-in *storage* (user/build params))
        [_ err] (sut/call (ctx) params)]
    (t/testing "error"
      (t/is (= :already-registered (:type err))))
    (t/testing "not sign in"
      (t/is (session/logged-out? *session*)))))

(t/deftest already-logged-in
  (let [params  (sgen/generate (s/gen ::user/build-params))
        user    (storage/create-agg-in *storage* (user/build params))
        user-id (:id user)
        _       (session/log-in! *session* user-id)
        [_ err] (sut/call (ctx) params)]
    (t/testing "error"
      (t/is (= :already-logged-in (:type err))))))

(t/deftest invalid-params
  (let [params  {}
        [_ err] (sut/call (ctx) params)]
    (t/testing "error"
      (t/is (= :invalid-params (:type err)))
      (t/is (contains? err :explain-data)))))
