(ns publicator.core.use-cases.aggregates.user-test
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.aggregates.user :as user]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest has-no-errors
  (let [agg (-> (user/build [{:db/ident             :root
                              :user/state           :active
                              :user/login           "john"
                              :user/password        "some password"
                              :user/password-digest "some digest"
                              :user/admin?          true
                              :user/author?         true}
                             {:translation/root              :root
                              :translation/lang              :en
                              :author.translation/first-name "John"
                              :author.translation/last-name  "Doe"}
                             {:translation/root              :root
                              :translation/lang              :ru
                              :author.translation/first-name "Иван"
                              :author.translation/last-name  "Иванов"}
                             {:author.achivement/root        :root
                              :author.achivement/kind        :star
                              :author.achivement/assigner-id 42}])
                (user/validate))]
    (t/is (agg/has-no-errors? agg))))
