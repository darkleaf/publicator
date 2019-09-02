(ns publicator.use-cases.user.register2-test
  (:require
   [publicator.use-cases.user.register2 :as register]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :refer [linearize]]
   [clojure.core.match :as m]
   [clojure.test :as t]))

(t/deftest process-success
  (linearize
   (let [msgs [[:user/login :add :root "john"]
               [:user/password :add :root "password"]]])

   (m/match (register/process msgs)
     {:get-session {:callback bind-session}})

   (m/match (bind-session {})
     {:get-user-presence-by-login {:login    "john"
                                   :callback bind-user-presence}})

   (m/match (bind-user-presence false)
     {:get-password-digest {:password "password"
                            :callback bind-password-digest}})

   (m/match (bind-password-digest "digest")
     {:get-user-id {:callback bind-user-id}})

   (let [effects          (bind-user-id 1)
         expected-effects {:set-session {:current-user-id 1}
                           :persist     [(-> user/new-blank
                                             (agg/with-msgs msgs)
                                             (agg/with-msgs
                                               [[:agg/id :add :root 1]
                                                [:user/password-digest :add :root "digest"]
                                                [:user/state :add :root :active]]))]
                           :reaction    {:type :show-screen
                                         :name :main}}])
   (t/is (= expected-effects effects))))

(t/deftest process-additional-msgs
  (linearize
   (let [msgs [[:user/login :add :root "john"]
               [:user/password :add :root "password"]
               [:user/state :add :root :archived]]])

   (let [effects          (register/process msgs)
         expected-effects {:reaction {:type :show-additional-messages-error
                                      :msgs #{:user/state}}}])
   (t/is (= expected-effects effects))))

(t/deftest process-already-logged-in
  (linearize
   (let [msgs [[:user/login :add :root "john"]
               [:user/password :add :root "password"]]])

   (m/match (register/process msgs)
     {:get-session {:callback bind-session}})

   (let [effects          (bind-session {:current-user-id 1})
         expected-effects {:reaction {:type :show-screen
                                      :name :main}}])
   (t/is (= expected-effects effects))))

(t/deftest process-already-registered
  (linearize
   (let [msgs [[:user/login :add :root "john"]
               [:user/password :add :root "password"]]])

   (m/match (register/process msgs)
     {:get-session {:callback bind-session}})

   (m/match (bind-session {})
     {:get-user-presence-by-login {:login    "john"
                                   :callback bind-user-presence}})

   (let [effects          (bind-user-presence true)
         expected-effects {:reaction {:type :show-screen
                                      :name :main}}])
   (t/is (= expected-effects effects))))

(t/deftest process-with-errr
  (linearize
   (let [msgs [[:user/login :add  :root "john"]]])

   (m/match (register/process msgs)
     {:get-session {:callback bind-session}})

   (m/match (bind-session {})
     {:get-user-presence-by-login {:login    "john"
                                   :callback bind-user-presence}})

   (m/match (bind-user-presence false)
     {:get-password-digest {:password nil
                            :callback bind-password-digest}})

   (let [effects          (bind-password-digest "digest")
         expected-effects {:reaction {:type   :show-validation-errors
                                      :errors #{{:error/type   :required
                                                 :error/entity 1
                                                 :error/attr   :user/password
                                                 :error/rule   'root}}}}])
   (t/is (= expected-effects effects))))
