(ns publicator.core.use-cases.user.register-test
  (:require
   [publicator.core.use-cases.user.register :as register]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest form-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::register/form (agg/allocate)]}]
        continuation (e/continuation register/form)]
    (script/test continuation script)))

(t/deftest form-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::register/already-logged-in]}]
        continuation (e/continuation register/form)]
    (script/test continuation script)))

(t/deftest process-success
  (let [form         (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"})
        user         (agg/allocate {:db/ident             :root
                                    :user/login           "john"
                                    :user/password-digest "digest"
                                    :user/role            :regular
                                    :user/state           :active})
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
                      {:effect   [:session/modify assoc ::user-session/id user-id]
                       :coeffect {::user-session/id user-id}}
                      {:final-effect [::register/processed persisted]}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [form         (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::register/already-logged-in]}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))

(t/deftest process-invalid-form
  (let [continuation (e/continuation register/process)]
    (t/are [form invalid] (script/test continuation
                                       [{:args [form]}
                                        {:effect   [:session/get]
                                         :coeffect {}}
                                        {:final-effect [::register/invalid-form invalid]}])
      (agg/allocate {:db/ident :root})
      (agg/allocate {:db/ident :root}
                    {:error/entity :root
                     :error/type   :required
                     :error/attr   :user/login}
                    {:error/entity :root
                     :error/type   :required
                     :error/attr   :user/password})

      (agg/allocate {:db/ident      :root
                     :user/login    ""
                     :user/password ""})
      (agg/allocate {:db/ident      :root
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
  (let [form         (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"})
        invalid      (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"}
                                   {:error/entity :root
                                    :error/type   ::register/existed-login
                                    :error/attr   :user/login
                                    :error/value  "john"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:persistence.user/exists-by-login "john"]
                       :coeffect true}
                      {:final-effect [::register/invalid-form invalid]}]
        continuation (e/continuation register/process)]
     (script/test continuation script)))
