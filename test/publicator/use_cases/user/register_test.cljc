(ns publicator.use-cases.user.register-test
  (:require
   [publicator.use-cases.user.register :as register]
   [publicator.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [clojure.test :as t]))

(t/deftest process-success
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui.form/edit (agg/allocate :agg/user)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"}]}
                      {:effect   [:persistence.user/get-by-login "john"]
                       :coeffect nil}
                      {:effect   [:hasher/derive "password"]
                       :coeffect "digest"}
                      {:effect   [:persistence/next-id :user]
                       :coeffect 1}
                      {:effect   [:session/assoc :current-user-id 1]
                       :coeffect nil}
                      {:effect   [:persistence/save
                                  (-> (agg/allocate :agg/new-user)
                                      (agg/apply-tx [{:db/ident             :root
                                                      :agg/id               1
                                                      :user/login           "john"
                                                      :user/password        "password"
                                                      :user/password-digest "digest"
                                                      :user/state           :active
                                                      :user/role            :regular}]))]
                       :coeffect nil}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation register/process)]
    (e/test continuation script)))

(t/deftest process-additional-attrs
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui.form/edit (agg/allocate :agg/user)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"
                                   :user/state    :archived}]}
                      {:final-effect [:ui.error/show :additional-attributes #{:user/state}]}]
        continuation (e/continuation register/process)]
    (e/test continuation script)))

(t/deftest process-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation register/process)]
    (e/test continuation script)))

(t/deftest process-already-registered
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui.form/edit (agg/allocate :agg/user)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"}]}
                      {:effect   [:persistence.user/get-by-login "john"]
                       :coeffect :fake/some-persisted-user}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation register/process)]
    (e/test continuation script)))

(t/deftest process-with-errr
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui.form/edit (agg/allocate :agg/user)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password ""}]}
                      {:effect   [:persistence.user/get-by-login "john"]
                       :coeffect nil}
                      {:effect   [:hasher/derive ""]
                       :coeffect "digest"}
                      {:final-effect [:ui.error/show :validation #{{:error/type   :predicate
                                                                    :error/entity 1
                                                                    :error/attr   :user/password
                                                                    :error/value  ""
                                                                    :error/pred   (str #".{8,255}")
                                                                    :error/rule   'root}}]}]
        continuation (e/continuation register/process)]
    (e/test continuation script)))
