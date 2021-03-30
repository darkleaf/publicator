(ns publicator.core.use-cases.interactors.user.update-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :as gen]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.aggregates.user :as user]
   [publicator.core.use-cases.interactors.test-common :as tc]
   [publicator.core.use-cases.interactors.user.update :as user.update]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest form-success
  (let [user-id 1
        user    (user/build {:db/ident             :root
                             :agg/id               user-id
                             :user/login           "john"
                             :user/password-digest "digest"
                             :user/state           :active})
        form    (agg/build {:db/ident   :root
                            :user/login "john"
                            :user/state :active})

        f*  (tc/wrap #'user.update/form*)
        ctx {:session {::user-session/id user-id}}
        gen (f* ctx user-id)]
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect ::user.update/->form form)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest form-not-authorized
  (let [user-id       1
        user          (agg/build {:db/ident             :root
                                  :agg/id               user-id
                                  :user/login           "john"
                                  :user/password-digest "digest"
                                  :user/state           :active})
        other-user-id 2
        other-user    (agg/build {:db/ident             :root
                                  :agg/id               other-user-id
                                  :user/login           "other-john"
                                  :user/password-digest "digest"
                                  :user/state           :active})

        f*  (tc/wrap #'user.update/form*)
        ctx {:session {::user-session/id user-id}}
        gen (f* ctx other-user-id)]
    (t/is (= (effect :persistence.user/get-by-id other-user-id)
             (gen/value gen)))
    (gen/next gen other-user)
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect ::user.update/->not-authorized)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest form-user-not-found
  (let [user-id 1

        f*  (tc/wrap #'user.update/form*)
        ctx {}
        gen (f* ctx user-id)]
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen nil)
    (t/is (= (effect ::user.update/->user-not-found)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest process-success
  (let [user-id   1
        form      (agg/build {:db/ident      :root
                              :user/login    "john"
                              :user/password "new password"
                              :user/state    :active
                              :user/author?  true}
                             {:translation/root              :root
                              :translation/lang              :en
                              :author.translation/first-name "John"
                              :author.translation/last-name  "Doe"}
                             {:translation/root              :root
                              :translation/lang              :ru
                              :author.translation/first-name "Иван"
                              :author.translation/last-name  "Иванов"})
        user      (agg/build {:db/ident             :root
                              :agg/id               user-id
                              :user/login           "john"
                              :user/password-digest "digest"
                              :user/state           :active})
        persisted (agg/build {:db/ident             :root
                              :agg/id               user-id
                              :user/login           "john"
                              :user/password-digest "new digest"
                              :user/state           :active
                              :user/author?         true}
                             {:translation/root              :root
                              :translation/lang              :en
                              :author.translation/first-name "John"
                              :author.translation/last-name  "Doe"}
                             {:translation/root              :root
                              :translation/lang              :ru
                              :author.translation/first-name "Иван"
                              :author.translation/last-name  "Иванов"})

        f*  (tc/wrap #'user.update/process*)
        ctx {:session {::user-session/id user-id}}
        gen (f* ctx user-id form)]
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect :persistence.user/get-by-id user-id)
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect :hasher/derive "new password")
             (gen/value gen)))
    (gen/next gen "new digest")
    (t/is (= (effect :persistence.user/update persisted)
             (gen/value gen)))
    (gen/next gen persisted)
    (t/is (= (effect ::user.update/->processed persisted)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest process-invalid-form
  (let [form         (agg/build {:db/ident :root})
        with-errors  (agg/build #:error{:attr   :user/login
                                        :entity :root
                                        :type   :required}
                                #:error{:attr   :user/state
                                        :entity :root
                                        :type   :required})

        f*  (tc/wrap #'user.update/process*)
        ctx {:session {::user-session/id 1}}
        gen (f* ctx 1 form)]
    (t/is (= (effect ::user.update/->invalid-form with-errors)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))
