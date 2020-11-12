(ns publicator.core.use-cases.interactors.user.register-test
  (:require
   [clojure.template :refer [do-template]]
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :as gen]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.interactors.user.register :as user.register]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.interactors.test-common :as tc]))

(t/deftest form-success
  (let [f*  (tc/wrap #'user.register/form*)
        ctx {}
        gen (f* ctx)]
    (t/is (= (effect ::user.register/->form (agg/build))
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest form-already-logged-in
  (let [f*  (tc/wrap #'user.register/form*)
        ctx {:session {::user-session/id 1}}
        gen (f* ctx)]
    (t/is (= (effect ::user.register/->already-logged-in)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest process-success
  (let [form      (agg/build {:db/ident      :root
                              :user/login    "john"
                              :user/password "password"})
        user      (agg/build {:db/ident             :root
                              :user/login           "john"
                              :user/password-digest "digest"
                              :user/state           :active})
        user-id   1
        persisted (d/db-with user [[:db/add :root :agg/id user-id]])

        f*  (tc/wrap #'user.register/process*)
        ctx {}
        gen (f* ctx form)]
    (t/is (= (effect :persistence.user/exists-by-login "john")
             (gen/value gen)))
    (gen/next gen false)
    (t/is (= (effect :hasher/derive "password")
             (gen/value gen)))
    (gen/next gen "digest")
    (t/is (= (effect :persistence.user/create user)
             (gen/value gen)))
    (gen/next gen persisted)
    (t/is (= (effect ::user.register/->processed persisted)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [{:session {::user-session/id 1}} nil]
             (gen/value gen)))))

(t/deftest process-already-logged-in
  (let [form (agg/build {:db/ident      :root
                         :user/login    "john"
                         :user/password "password"})

        f*  (tc/wrap #'user.register/process*)
        ctx {:session {::user-session/id 1}}
        gen (f* ctx form)]
    (t/is (= (effect ::user.register/->already-logged-in)
             (gen/value gen)))

    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest process-invalid-form
  (let [f*  (tc/wrap #'user.register/process*)
        ctx {}]
    (do-template [form invalid] (let [gen (f* ctx form)]
                                  (t/is (= (effect ::user.register/->invalid-form
                                                   invalid)
                                           (gen/value gen)))
                                  (gen/return gen)
                                  (t/is (gen/done? gen))
                                  (t/is (= [ctx nil] (gen/value gen))))
      (agg/build {:db/ident :root})
      (agg/build {:db/ident :root}
                 {:error/entity :root
                  :error/type   :required
                  :error/attr   :user/login}
                 {:error/entity :root
                  :error/type   :required
                  :error/attr   :user/password})

      (agg/build {:db/ident      :root
                  :user/login    ""
                  :user/password ""})
      (agg/build {:db/ident      :root
                  :user/login    ""
                  :user/password ""}
                 {:error/entity :root
                  :error/type   :predicate
                  :error/attr   :user/login
                  :error/value  ""}
                 {:error/entity :root
                  :error/type   :predicate
                  :error/attr   :user/password
                  :error/value  ""}))))

(t/deftest process-existed-login
  (let [form    (agg/build {:db/ident      :root
                            :user/login    "john"
                            :user/password "password"})
        invalid (agg/build {:db/ident      :root
                            :user/login    "john"
                            :user/password "password"}
                           {:error/entity :root
                            :error/type   ::user.register/existed-login
                            :error/attr   :user/login
                            :error/value  "john"})

        f*  (tc/wrap #'user.register/process*)
        ctx {}
        gen (f* ctx form)]
    (t/is (= (effect :persistence.user/exists-by-login "john")
             (gen/value gen)))
    (gen/next gen true)
    (t/is (= (effect ::user.register/->invalid-form invalid)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))))
