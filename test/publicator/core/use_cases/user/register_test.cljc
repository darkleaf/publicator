(ns publicator.core.use-cases.user.register-test
  (:require
   [publicator.core.use-cases.user.register :as register]
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [clojure.test :as t]))

(t/deftest process-success
  (let [script [{:args []}
                {:effect   [:session/get]
                 :coeffect {}}

                {:effect   [:ui.form/edit (agg/allocate :form.user/register)]
                 :coeffect [{:db/ident   :root
                             :user/login "john"}]}
                {:effect   [:persistence.user/exists-by-login "john"]
                 :coeffect true}


                {:effect   [:ui.form/edit
                            (-> (agg/allocate :form.user/register)
                                (agg/apply-tx [{:db/ident   :root
                                                :user/login "john"}
                                               {:db/id        2
                                                :error/attr   :user/password
                                                :error/entity :root
                                                :error/rule   'root
                                                :error/type   :required}
                                               {:db/id        3
                                                :error/attr   :user/login
                                                :error/entity :root
                                                :error/type   ::register/existed-login
                                                :error/value  "john"}]))]
                 :coeffect [{:db/ident      :root
                             :user/login    "john_doe"
                             :user/password "password"}]}
                {:effect   [:persistence.user/exists-by-login "john_doe"]
                 :coeffect false}

                {:effect   [:hasher/derive "password"]
                 :coeffect "digest"}
                {:effect   [:persistence/next-id :user]
                 :coeffect 1}
                {:effect [:persistence/save
                          (-> (agg/allocate :form.user/register)
                              (agg/apply-tx [{:db/ident             :root
                                              :agg/id               1
                                              :user/login           "john_doe"
                                              :user/password        "password"
                                              :user/password-digest "digest"
                                              :user/role            :regular
                                              :user/state           :active}]))]}
                {:effect   [:session/assoc :current-user-id 1]
                 :coeffect nil}
                {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))

(t/deftest process-additional-attrs
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui.form/edit (agg/allocate :form.user/register)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"
                                   :user/state    :archived}]}
                      {:throw (ex-info "Additional datoms"
                                       {:additional [(agg/datom 1 :user/state :archived)]})}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))
