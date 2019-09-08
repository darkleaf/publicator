(ns publicator.use-cases.user.register2-test
  (:require
   [publicator.use-cases.user.register2 :as register]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :refer [linearize]]
   [clojure.core.match :as m]
   [clojure.test :as t]))

(defn- check-with-script [script fn & args]
  (loop [[effect callback]          (apply fn args)
         [[expected result] & s-tail] script]
    (t/is (= expected effect))
    (if (nil? callback)
      (t/is (empty? s-tail))
      (recur (callback result)
             s-tail))))

(t/deftest process-success
  (let [msgs   [[:user/login :add :root "john"]
                [:user/password :add :root "password"]]
        script [[[:get-session] {}]
                [[:get-user-presence-by-login "john"] false]
                [[:get-password-digest "password"] "digest"]
                [[:get-new-user-id] 1]
                [[:do
                  [:set-session {:current-user-id 1}]
                  [:persist (-> user/new-blank
                                (agg/with-msgs msgs)
                                (agg/with-msgs
                                  [[:agg/id :add :root 1]
                                   [:user/password-digest :add :root "digest"]
                                   [:user/state :add :root :active]]))]]
                 [nil nil]]
                [[:show-screen :main]]]]
    (check-with-script script register/process msgs)))

(t/deftest process-additional-msgs
  (let [msgs   [[:user/login :add :root "john"]
                [:user/password :add :root "password"]
                [:user/state :add :root :archived]]
        script [[[:show-additional-messages-error #{:user/state}]]]]
    (check-with-script script register/process msgs)))

(t/deftest process-already-logged-in
  (let [msgs   [[:user/login :add :root "john"]
                [:user/password :add :root "password"]]
        script [[[:get-session] {:current-user-id 1}]
                [[:show-screen :main]]]]
    (check-with-script script register/process msgs)))

(t/deftest process-already-registered
  (let [msgs   [[:user/login :add :root "john"]
                [:user/password :add :root "password"]]
        script [[[:get-session] {}]
                [[:get-user-presence-by-login "john"] true]
                [[:show-screen :main]]]]
    (check-with-script script register/process msgs)))

(t/deftest process-with-errr
  (let [msgs   [[:user/login :add :root "john"]
                [:user/password :add :root ""]]
        script [[[:get-session] {}]
                [[:get-user-presence-by-login "john"] false]
                [[:get-password-digest ""] "digest"]
                [[:show-validation-errors #{{:error/type      :predicate
                                             :error/entity    1
                                             :error/attr      :user/password
                                             :error/value     ""
                                             :error/pred-name "#\".{8,255}\""
                                             :error/rule      'root}}]]]]
    (check-with-script script register/process msgs)))
