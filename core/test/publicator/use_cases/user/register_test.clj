(ns publicator.use-cases.user.register-test
  (:require
   [publicator.use-cases.user.register :as register]
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]
   [darkleaf.effect.core :as e]
   [clojure.test :as t]))

(t/deftest process-success
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:effect   [:persistence/user-presence-by-login "john"]
                  :coeffect false}
                 {:effect   [:hasher/derive "password"]
                  :coeffect "digest"}
                 {:effect   [:persistence/next-user-id]
                  :coeffect 1}
                 {:effect   [:session/assoc :current-user-id 1]
                  :coeffect nil}
                 {:effect   [:persistence/save (-> (agg/allocate :agg/new-user)
                                                   (agg/apply-tx tx-data)
                                                   (agg/apply-tx [{:db/ident             :root
                                                                   :agg/id               1
                                                                   :user/password-digest "digest"
                                                                   :user/state           :active
                                                                   :user/role            :regular}]))]
                  :coeffect nil}
                 {:final-effect [:ui/show-main-screen]}]]
    (e/test register/process script)))

(t/deftest process-additional-attrs
  (let [script [{:args [[{:db/ident      :root
                          :user/login    "john"
                          :user/password "password"
                          :user/state    :archived}]]}
                {:effect   [:session/get]
                 :coeffect {}}
                {:final-effect [:ui/show-additional-attributes-error #{:user/state}]}]]
    (e/test register/process script)))

(t/deftest process-already-logged-in
  (let [script [{:args [[{:db/ident      :root
                          :user/login    "john"
                          :user/password "password"}]]}
                {:effect   [:session/get]
                 :coeffect {:current-user-id 1}}
                {:final-effect [:ui/show-main-screen]}]]
    (e/test register/process script)))

(t/deftest process-already-registered
  (let [script [{:args [[{:db/ident      :root
                          :user/login    "john"
                          :user/password "password"}]]}
                {:effect   [:session/get]
                 :coeffect {}}
                {:effect   [:persistence/user-presence-by-login "john"]
                 :coeffect true}
                {:final-effect [:ui/show-main-screen]}]]
    (e/test register/process script)))

(t/deftest process-with-errr
  (let [script [{:args [[{:db/ident      :root
                          :user/login    "john"
                          :user/password ""}]]}
                {:effect   [:session/get]
                 :coeffect {}}
                {:effect   [:persistence/user-presence-by-login "john"]
                 :coeffect false}
                {:effect   [:hasher/derive ""]
                 :coeffect "digest"}
                {:final-effect [:ui/show-validation-errors #{{:error/type   :predicate
                                                              :error/entity 1
                                                              :error/attr   :user/password
                                                              :error/value  ""
                                                              :error/pred   ".{8,255}"
                                                              :error/rule   'root}}]}]]
    (e/test register/process script)))
