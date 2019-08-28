(ns publicator.use-cases.user.register-test
  (:require
   [publicator.use-cases.user.register :as register]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [clojure.test :as t]))

(t/deftest process-success
  (let [msgs                 [[:user/login :add :root "john"]
                              [:user/password :add :root "password"]]
        session              {}
        pass->digest         {"password" "digest"}
        login->user-presence {"john" false}
        new-user-ids         [1]
        effects              (register/process msgs
                                               session
                                               login->user-presence
                                               pass->digest
                                               new-user-ids)
        expected-effects     {:set-session {:current-user-id 1}
                              :persist     [(-> user/new-blank
                                                (agg/with-msgs msgs)
                                                (agg/with-msgs
                                                  [[:agg/id :add :root 1]
                                                   [:user/password-digest :add :root "digest"]
                                                   [:user/state :add :root :active]]))]
                              :reaction    {:type :show-screen
                                            :name :main}}]
    (t/is (= expected-effects effects))))

(t/deftest process-additional-msgs
  (let [msgs                 [[:user/login :add :root "john"]
                              [:user/password :add :root "pass"]
                              [:user/state :add :root :archived]]
        session              {}
        pass->digest         {"pass" "digest"}
        login->user-presence {"john" false}
        new-user-ids         [1]
        effects              (register/process msgs
                                               session
                                               login->user-presence
                                               pass->digest
                                               new-user-ids)
        expected-effects     {:reaction {:type :show-additional-messages-error
                                         :msgs #{:user/state}}}]
    (t/is (= expected-effects effects))))

(t/deftest process-with-errr
  (let [msgs                 [[:user/password :add :root "password"]]
        session              {}
        pass->digest         {"password" "digest"}
        login->user-presence {"john" false}
        new-user-ids         [1]
        effects              (register/process msgs
                                               session
                                               login->user-presence
                                               pass->digest
                                               new-user-ids)
        expected-effects     {:reaction {:type   :show-validation-errors
                                         :errors #{{:error/type   :required
                                                    :error/entity 1
                                                    :error/attr   :user/login
                                                    :error/rule   'root}}}}]
    (t/is (= expected-effects effects))))

(t/deftest process-already-logged-in
  (let [msgs                 [[:user/login :add :root "john"]
                              [:user/password :add :root "password"]]
        session              {:current-user-id 1}
        pass->digest         {"password" "digest"}
        login->user-presence {"john" false}
        new-user-ids         [2]
        effects              (register/process msgs
                                               session
                                               login->user-presence
                                               pass->digest
                                               new-user-ids)
        expected-effects     {:reaction {:type :show-screen
                                         :name :main}}]
    (t/is (= expected-effects effects))))
