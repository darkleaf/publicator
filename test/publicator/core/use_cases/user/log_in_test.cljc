(ns publicator.core.use-cases.user.log-in-test
  (:require
   [publicator.core.use-cases.user.log-in :as log-in]
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [clojure.test :as t]))

(t/deftest process-success
  (let [user   (-> (agg/allocate :agg/user)
                   (agg/apply-tx [{:db/ident             :root
                                   :agg/id               1
                                   :user/login           "john"
                                   :user/password-digest "digest"
                                   :user/state           :active}]))
        script [{:args []}
                {:effect   [:session/get]
                 :coeffect {}}

                {:effect   [:ui.form/edit (agg/allocate :form.user/log-in)]
                 :coeffect [{:db/ident :root}]}
                {:effect   [:ui.form/edit
                            (-> (agg/allocate :form.user/log-in)
                                (agg/apply-tx [{:db/ident :root}
                                               {:db/id        2
                                                :error/attr   :user/password
                                                :error/entity 1
                                                :error/rule   'root
                                                :error/type   :required}
                                               {:db/id        3
                                                :error/attr   :user/login
                                                :error/entity 1
                                                :error/rule   'root
                                                :error/type   :required}]))]
                 :coeffect [{:db/ident      :root
                             :user/login    "wrong_john"
                             :user/password "password"}]}
                {:effect   [:persistence.user/get-by-login "wrong_john"]
                 :coeffect nil}

                {:effect [:ui.form/edit
                          (-> (agg/allocate :form.user/log-in)
                              (agg/apply-tx [{:db/ident      :root
                                              :user/login    "wrong_john"
                                              :user/password "password"}
                                             {:db/id        4
                                              :error/entity :root
                                              :error/type   ::log-in/wrong-login-or-password}]))]

                 :coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "wrong_password"}]}
                {:effect   [:persistence.user/get-by-login "john"]
                 :coeffect user}
                {:effect   [:hasher/check "wrong_password" "digest"]
                 :coeffect false}

                {:effect [:ui.form/edit
                          (-> (agg/allocate :form.user/log-in)
                              (agg/apply-tx [{:db/ident      :root
                                              :user/login    "john"
                                              :user/password "wrong_password"}
                                             {:db/id        5
                                              :error/entity :root
                                              :error/type   ::log-in/wrong-login-or-password}]))]

                 :coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"}]}
                {:effect   [:persistence.user/get-by-login "john"]
                 :coeffect user}
                {:effect   [:hasher/check "password" "digest"]
                 :coeffect true}

                {:effect   [:persistence.user/get-by-login "john"]
                 :coeffect user}
                {:effect   [:session/assoc :current-user-id 1]
                 :coeffect nil}
                {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation log-in/process)]
    (script/test continuation script)))

(t/deftest process-additional-attrs
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui.form/edit (agg/allocate :form.user/log-in)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"
                                   :user/extra    :value}]}
                      {:throw (ex-info "Additional datoms"
                                       {:additional [(agg/datom 1 :user/extra :value)]})}]
        continuation (e/continuation log-in/process)]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation log-in/process)]
    (script/test continuation script)))
