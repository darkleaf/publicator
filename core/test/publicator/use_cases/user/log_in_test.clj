(ns publicator.use-cases.user.log-in-test
  (:require
   [publicator.use-cases.user.log-in :as log-in]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [clojure.test :as t]))

(t/deftest process-success
  (let [msgs             [[:user/login :add :root "john"]
                          [:user/password :add :root "password"]]
        session          {}
        user             (-> user/blank
                             (agg/with-msgs [[:agg/id :add :root 1]
                                             [:user/login :add :root "john"]
                                             [:user/password-digest :add :root "digest"]
                                             [:user/state :add :root :active]])
                             (agg/validate!))
        login->user      {"john" user}
        check-password   (fn [attempt encrypted]
                           (and (= "password" attempt)
                                (= "digest" encrypted)))
        effects          (log-in/process msgs
                                         session
                                         login->user
                                         check-password)
        expected-effects {:set-session {:current-user-id 1}
                          :reaction    {:type :show-screen
                                        :name :main}}]
    (t/is (= expected-effects effects))))

(t/deftest process-not-found
  (let [msgs             [[:user/login :add :root "john"]
                          [:user/password :add :root "password"]]
        session          {}
        login->user      {}
        check-password   (constantly false)
        effects          (log-in/process msgs
                                         session
                                         login->user
                                         check-password)
        expected-effects {:reaction {:type :show-not-found-error}}]
    (t/is (= expected-effects effects))))

(t/deftest process-wrong-password
  (let [msgs             [[:user/login :add :root "john"]
                          [:user/password :add :root "wrong-password"]]
        session          {}
        user             (-> user/blank
                             (agg/with-msgs [[:agg/id :add :root 1]
                                             [:user/login :add :root "john"]
                                             [:user/password-digest :add :root "digest"]
                                             [:user/state :add :root :active]])
                             (agg/validate!))
        login->user      {"john" user}
        check-password   (fn [attempt encrypted]
                           (and (= "password" attempt)
                                (= "digest" encrypted)))
        effects          (log-in/process msgs
                                         session
                                         login->user
                                         check-password)
        expected-effects {:reaction {:type :show-wrong-password-error}}]
    (t/is (= expected-effects effects))))

(t/deftest process-additional-msgs
  (let [msgs             [[:user/login :add :root "john"]
                          [:user/password :add :root "password"]
                          [:user/extra :add :root :value]]
        session          {}
        login->user      {}
        check-password   (constantly false)
        effects          (log-in/process msgs
                                         session
                                         login->user
                                         check-password)
        expected-effects {:reaction {:type :show-additional-messages-error
                                     :msgs #{:user/extra}}}]
    (t/is (= expected-effects effects))))

(t/deftest process-with-errr
  (let [msgs             [[:user/password :add :root "password"]]
        session          {}
        login->user      {}
        check-password   (constantly false)
        effects          (log-in/process msgs
                                         session
                                         login->user
                                         check-password)
        expected-effects {:reaction {:type   :show-validation-errors
                                     :errors #{{:error/type   :required
                                                :error/entity 1
                                                :error/attr   :user/login
                                                :error/rule   'root}}}}]
    (t/is (= expected-effects effects))))

(t/deftest process-already-logged-in
  (let [msgs             [[:user/login :add :root "john"]
                          [:user/password :add :root "password"]]
        session          {:current-user-id 1}
        login->user      {}
        check-password   (constantly false)
        effects          (log-in/process msgs
                                         session
                                         login->user
                                         check-password)
        expected-effects {:reaction {:type :show-screen
                                     :name :main}}]
    (t/is (= expected-effects effects))))
