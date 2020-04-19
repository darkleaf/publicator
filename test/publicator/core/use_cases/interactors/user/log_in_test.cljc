(ns publicator.core.use-cases.interactors.user.log-in-test
  (:require
   [publicator.core.use-cases.interactors.user.log-in :as log-in]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [clojure.test :as t]
   [datascript.core :as d]))

(t/deftest form-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:final-effect [::log-in/->form (agg/allocate)]}]
        continuation (e/continuation log-in/form)]
    (script/test continuation script)))

(t/deftest form-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::log-in/->already-logged-in]}]
        continuation (e/continuation log-in/form)]
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
        continuation (e/continuation log-in/process)]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [form         (agg/allocate {:db/ident      :root
                                    :user/login    "john"
                                    :user/password "password"})
        script       [{:args [form]}
                      {:effect   [:session/get]
                       :coeffect {::user-session/id 1}}
                      {:final-effect [::log-in/->already-logged-in]}]
        continuation (e/continuation log-in/process)]
    (script/test continuation script)))
