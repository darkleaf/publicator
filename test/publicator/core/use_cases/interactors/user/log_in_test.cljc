(ns publicator.core.use-cases.interactors.user.log-in-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :refer [effect]]
   [darkleaf.generator.core :as gen]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.interactors.test-common :as tc]
   [publicator.core.use-cases.interactors.user.log-in :as user.log-in]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest form-success
  (let [f*  (tc/wrap #'user.log-in/form*)
        ctx {}
        gen (f* ctx)]
    (t/is (= (effect ::user.log-in/->form (agg/build))
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest form-already-logged-in
  (let [f*  (tc/wrap #'user.log-in/form*)
        ctx {:session {::user-session/id 1}}
        gen (f* ctx)]
    (t/is (= (effect ::user.log-in/->already-logged-in)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest process-success
  (let [form    (agg/build {:db/ident      :root
                            :user/login    "john"
                            :user/password "password"})
        user-id 1
        user    (agg/build {:db/ident             :root
                            :agg/id               user-id
                            :user/login           "john"
                            :user/password-digest "digest"
                            :user/state           :active})

        f*  (tc/wrap #'user.log-in/process*)
        ctx {}
        gen (f* ctx form)]
    (t/is (= (effect :persistence.user/get-by-login "john")
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect :hasher/check "password" "digest")
             (gen/value gen)))
    (gen/next gen true)
    (t/is (= (effect :persistence.user/get-by-login "john")
             (gen/value gen)))
    (gen/next gen user)
    (t/is (= (effect ::user.log-in/->processed)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [{:session {::user-session/id user-id}} nil]
             (gen/value gen)))))

(t/deftest process-already-logged-in
  (let [form (agg/build {:db/ident      :root
                         :user/login    "john"
                         :user/password "password"})

        f*  (tc/wrap #'user.log-in/process*)
        ctx {:session {::user-session/id 1}}
        gen (f* ctx form)]
    (t/is (= (effect ::user.log-in/->already-logged-in)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))

(t/deftest process-invalid-form
  (let [form        (agg/build {:db/ident :root})
        with-errors (agg/build #:error{:attr   :user/login
                                       :entity :root
                                       :type   :required}
                               #:error{:attr   :user/password
                                       :entity :root
                                       :type   :required})

        f*  (tc/wrap #'user.log-in/process*)
        ctx {}
        gen (f* ctx form)]
    (t/is (= (effect ::user.log-in/->invalid-form with-errors)
             (gen/value gen)))
    (gen/return gen)
    (t/is (gen/done? gen))
    (t/is (= [ctx nil] (gen/value gen)))))
