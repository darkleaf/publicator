(ns publicator.core.use-cases.interactors.user.log-in-test
  (:require
   [clojure.test :as t]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.middleware.contract :as contract]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.use-cases.interactors.user.log-in :as user.log-in]
   [publicator.core.use-cases.services.user-session :as user-session]))

(t/deftest form-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::user.log-in/->form (agg/build)]}]
        continuation (-> user.log-in/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-in/form))]
    (script/test continuation script)))

(t/deftest form-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::user.log-in/->already-logged-in]}]
        continuation (-> user.log-in/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-in/form))]
    (script/test continuation script)))

(t/deftest process-success
  (let [form         (agg/build {:db/ident      :root
                                 :user/login    "john"
                                 :user/password "password"})
        user-id      1
        user         (agg/build {:db/ident             :root
                                 :agg/id               user-id
                                 :user/login           "john"
                                 :user/password-digest "digest"
                                 :user/state           "active"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:persistence.user/get-by-login "john"]
                       :coeffect user}
                      {:effect   [:hasher/check "password" "digest"]
                       :coeffect true}
                      {:effect   [:persistence.user/get-by-login "john"]
                       :coeffect user}
                      {:effect   [:session/swap assoc ::user-session/id user-id]
                       :coeffect {::user-session/id user-id}}
                      {:final-effect [::user.log-in/->processed]}]
        continuation (-> user.log-in/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-in/process))]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [form         (agg/build {:db/ident      :root
                                 :user/login    "john"
                                 :user/password "password"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::user.log-in/->already-logged-in]}]
        continuation (-> user.log-in/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-in/process))]
    (script/test continuation script)))

(t/deftest process-invalid-form
  (let [form         (agg/build {:db/ident :root})
        with-errors  (agg/build #:error{:attr   :user/login
                                        :entity :root
                                        :type   :required}
                                #:error{:attr   :user/password
                                        :entity :root
                                        :type   :required})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::user.log-in/->invalid-form with-errors]}]
        continuation (-> user.log-in/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `user.log-in/process))]
    (script/test continuation script)))
