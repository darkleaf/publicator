(ns publicator.use-cases.interactors.user.register-test
  (:require
   [publicator.use-cases.interactors.user.register :as user.register]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.use-cases.abstractions.test-impl.scaffolding :as scaffolding]
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [publicator.utils.test :as u.t]
   [clojure.test :as t]))

(defn test-main []
  (let [tx-data [[:db/add 1 :user/login "john"]
                 [:db/add 1 :user/password "password"]]
        user   (user.register/process tx-data)]
    (t/testing "success"
      (t/is (some? user)))
    (t/testing "logged in"
      (t/is (user-session/logged-in?)))
    (t/testing "persisted"
      (t/is (some? (storage/transaction
                    (storage/*get* :user (agg/id user))))))))

(defn test-already-logged-in []
  (let [user    (-> (agg/build user/spec)
                    (agg/change [{:db/ident      :root
                                  :user/login    "john"
                                  :user/password "password"
                                  :user/state    :active}]
                                agg/allow-everething)
                    (agg/validate!))
        _       (storage/transaction
                 (storage/*create* user))
        _       (user-session/log-in! user)
        tx-data [{:db/ident      :root
                  :user/login    "aaaa"
                  :user/password "12345678"}]]
    (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Authorization failed"
                            (user.register/process tx-data)))))

(defn test-invalid-params []
  (t/is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Aggregate has errors"
                          (user.register/process []))))

(t/deftest register
  (u.t/run 'publicator.use-cases.interactors.user.register-test
    scaffolding/setup))
