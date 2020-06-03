(ns publicator.core.use-cases.interactors.user.register-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.register :as user.register]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest form-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::user.register/->form (agg/build)]}]
        continuation (-> user.register/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.register/form))]
    (script/test continuation script)))

(t/deftest form-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::user.register/->already-logged-in]}]
        continuation (-> user.register/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.register/form))]
    (script/test continuation script)))

(t/deftest process-success
  (let [form         (agg/build {:db/ident      :root
                                 :user/login    "john"
                                 :user/password "password"})
        user         (agg/build {:db/ident             :root
                                 :user/login           "john"
                                 :user/password-digest "digest"
                                 :user/state           "active"})
        user-id      1
        persisted    (d/db-with user [[:db/add :root :agg/id user-id]])
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:persistence.user/exists-by-login "john"]
                       :coeffect false}
                      {:effect   [:hasher/derive "password"]
                       :coeffect "digest"}
                      {:effect   [:persistence.user/create user]
                       :coeffect persisted}
                      {:effect   [:session/swap assoc ::user-session/id user-id]
                       :coeffect {::user-session/id user-id}}
                      {:final-effect [::user.register/->processed persisted]}]
        continuation (-> user.register/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.register/process))]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [form         (agg/build {:db/ident      :root
                                 :user/login    "john"
                                 :user/password "password"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::user.register/->already-logged-in]}]
        continuation (-> user.register/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.register/process))]
    (script/test continuation script)))

(t/deftest process-invalid-form
  (let [continuation (-> user.register/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.register/process))]
    (t/are [form invalid] (script/test continuation
                                       [{:args [form]}
                                        {:effect   [:session/get]
                                         :coeffect {}}
                                        {:final-effect [::user.register/->invalid-form invalid]}])
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
  (let [form         (agg/build {:db/ident      :root
                                 :user/login    "john"
                                 :user/password "password"})
        invalid      (agg/build {:db/ident      :root
                                 :user/login    "john"
                                 :user/password "password"}
                                {:error/entity :root
                                 :error/type   ::user.register/existed-login
                                 :error/attr   :user/login
                                 :error/value  "john"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:persistence.user/exists-by-login "john"]
                       :coeffect true}
                      {:final-effect [::user.register/->invalid-form invalid]}]
        continuation (-> user.register/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.register/process))]
    (script/test continuation script)))
