(ns publicator.interactors.user.register-test
  (:require
   [publicator.interactors.user.register :as sut]
   [publicator.interactors.abstractions.transaction :as tx]
   [publicator.fakes.storage :as fakes.storage]
   [publicator.interactors.abstractions.session :as session]
   [publicator.fakes.session :as fakes.session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.stubs.user-queries :as stubs.user-q]
   [publicator.domain.user :as user]
   [publicator.interactors.fixtures :as fixtures]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen])
  (:import [publicator.domain.user User]))

(declare ^:dynamic *session*)
(declare ^:dynamic *tx-factory*)

(defn setup [f]
  (binding [*session*    (fakes.session/build)
            *tx-factory* (fakes.storage/build-tx-factory)]
    (f)))

(t/use-fixtures :each setup fixtures/all)

(t/deftest main
  (let [params     (sgen/generate (s/gen ::user/build-params))
        ctx        {::tx/tx-factory       *tx-factory*
                    ::session/session     *session*
                    ::user-q/get-by-login (stubs.user-q/get-by-login nil)}
        [user err] (sut/call ctx params)
        user-id    (:id user)]
    (t/testing "no error"
      (t/is (nil? err)))
    (t/testing "sign in"
      (t/is (= user-id
               (session/user-id *session*))))
    (t/testing "persisted"
      (tx/with-tx [tx (tx/build *tx-factory*)]
        (let [user (tx/get-aggregate tx User user-id)]
          (t/is (= (:login params) (:login @user))))))))

(t/deftest already-registered
  (let [params  (sgen/generate (s/gen ::user/build-params))
        user    (-> (sgen/generate (s/gen ::user/attrs))
                    (assoc :login (:login params)))
        ctx     {::tx/tx-factory       *tx-factory*
                 ::session/session     *session*
                 ::user-q/get-by-login (stubs.user-q/get-by-login user)}
        [_ err] (sut/call ctx params)]
    (t/testing "error"
      (t/is (= :already-registered (:type err))))
    (t/testing "not sign in"
      (t/is (session/logged-out? *session*)))))

(t/deftest wrong-params
  (let [params  {}
        ctx     {::tx/tx-factory       *tx-factory*
                 ::session/session     *session*
                 ::user-q/get-by-login (stubs.user-q/get-by-login nil)}
        [_ err] (sut/call ctx params)]
    (t/testing "error"
      (t/is (= :invalid-params (:type err)))
      (t/is (contains? err :explain-data)))))

(t/deftest already-logged-in
  (let [params  (sgen/generate (s/gen ::user/build-params))
        user-id (sgen/generate (s/gen ::user/id))
        ctx     {::tx/tx-factory       *tx-factory*
                 ::session/session     *session*
                 ::user-q/get-by-login (stubs.user-q/get-by-login nil)}
        _       (session/log-in! *session* user-id)
        [_ err] (sut/call ctx params)]
    (t/testing "error"
      (t/is (= :already-logged-in (:type err))))))
