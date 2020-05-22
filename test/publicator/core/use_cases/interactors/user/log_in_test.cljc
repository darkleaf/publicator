(ns publicator.core.use-cases.interactors.user.log-in-test
  (:require
   [publicator.core.use-cases.interactors.user.log-in :as log-in]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.contracts :as contracts]
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [darkleaf.effect.middleware.contract :as contract]
   [clojure.test :as t]
   [datascript.core :as d]))

(t/deftest form-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::log-in/->form (agg/allocate)]}]
        continuation (-> log-in/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `log-in/form))]
    (script/test continuation script)))

(t/deftest form-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::log-in/->already-logged-in]}]
        continuation (-> log-in/form
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `log-in/form))]
    (script/test continuation script)))

(t/deftest process-success
  (let [form         (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"})
        user-id      1
        user         (agg/allocate {:db/ident             :root
                                    :agg/id               user-id
                                    :user/login           "john"
                                    :user/password-digest "digest"
                                    :user/state           :active})
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
                      {:final-effect [::log-in/->processed]}]
        continuation (-> log-in/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `log-in/process))]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [form         (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::log-in/->already-logged-in]}]
        continuation (-> log-in/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `log-in/process))]
    (script/test continuation script)))

(t/deftest process-invalid-form
  (let [form         (agg/allocate {:db/ident :root})
        with-errors  (agg/allocate #:error{:attr   :user/login
                                           :entity :root
                                           :type   :required}
                                   #:error{:attr   :user/password
                                           :entity :root
                                           :type   :required})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::log-in/->invalid-form with-errors]}]
        continuation (-> log-in/process
                         (e/continuation)
                         (contract/wrap-contract @contracts/registry `log-in/process))]
    (script/test continuation script)))
