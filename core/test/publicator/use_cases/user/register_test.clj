(ns publicator.use-cases.user.register-test
  (:require
   [publicator.use-cases.user.register :as register]
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]
   [clojure.test :as t]))

(t/deftest process-success
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:coeffect tx-data}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:effect   [:persistence/user-presence-by-login "john"]
                  :coeffect false}
                 {:effect   [:hasher/derive "password"]
                  :coeffect "digest"}
                 {:effect   [:persistence/next-user-id]
                  :coeffect 1}
                 {:effect [:do
                           [:session/assoc :current-user-id 1]
                           [:persistence/save (-> (agg/allocate :agg/new-user)
                                                  (agg/apply-tx tx-data)
                                                  (agg/apply-tx [{:db/ident             :root
                                                                  :agg/id               1
                                                                  :user/password-digest "digest"
                                                                  :user/state           :active
                                                                  :user/role            :regular}]))]
                           [:ui/show-main-screen]]}]]
    (u/test-with-script register/process script)))

(t/deftest process-additional-attrs
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"
                             :user/state    :archived}]}
                {:effect   [:session/get]
                 :coeffect {}}
                {:effect [:ui/show-additional-attributes-error #{:user/state}]}]]
    (u/test-with-script register/process script)))

(t/deftest process-already-logged-in
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"}]}
                {:effect   [:session/get]
                 :coeffect {:current-user-id 1}}
                {:effect [:ui/show-main-screen]}]]
    (u/test-with-script register/process script)))

(t/deftest process-already-registered
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"}]}
                {:effect   [:session/get]
                 :coeffect {}}
                {:effect   [:persistence/user-presence-by-login "john"]
                 :coeffect true}
                {:effect [:ui/show-main-screen]}]]
    (u/test-with-script register/process script)))

(t/deftest process-with-errr
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password ""}]}
                {:effect   [:session/get]
                 :coeffect {}}
                {:effect   [:persistence/user-presence-by-login "john"]
                 :coeffect false}
                {:effect   [:hasher/derive ""]
                 :coeffect "digest"}
                {:effect [:ui/show-validation-errors #{{:error/type   :predicate
                                                        :error/entity 1
                                                        :error/attr   :user/password
                                                        :error/value  ""
                                                        :error/pred   ".{8,255}"
                                                        :error/rule   'root}}]}]]
    (u/test-with-script register/process script)))
