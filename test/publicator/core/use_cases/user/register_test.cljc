(ns publicator.core.use-cases.user.register-test
  (:require
   [publicator.core.use-cases.user.register :as register]
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [darkleaf.effect.script :as script]
   [datascript.core :as d]
   [clojure.test :as t]))

(t/deftest process-success
  (let [script [{:args []}
                {:effect   [:session/get]
                 :coeffect {}}

                {:effect   [:ui.form/edit (agg/allocate)]
                 :coeffect [{:db/ident   :root
                             :user/login "john"}]}

                {:effect   [:ui.form/edit
                            (-> (agg/allocate)
                                (d/db-with [{:db/ident   :root
                                             :user/login "john"}
                                            {:db/id        2
                                             :error/attr   :user/password
                                             :error/entity :root
                                             :error/type   :required}]))]
                 :coeffect [{:db/ident      :root
                             :user/login    "wrong_john"
                             :user/password "password"}]}
                {:effect   [:persistence.user/exists-by-login "wrong_john"]
                 :coeffect true}

                {:effect   [:ui.form/edit
                            (-> (agg/allocate)
                                (d/db-with [{:db/ident      :root
                                             :user/login    "wrong_john"
                                             :user/password "password"}
                                            {:db/id        3
                                             :error/attr   :user/login
                                             :error/entity :root
                                             :error/type   ::register/existed-login
                                             :error/value  "wrong_john"}]))]
                 :coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"}]}

                {:effect   [:persistence.user/exists-by-login "john"]
                 :coeffect false}

                {:effect   [:hasher/derive "password"]
                 :coeffect "digest"}
                {:effect   [:persistence.user/next-id]
                 :coeffect 1}
                {:effect [:persistence.user/create
                          (-> (agg/allocate)
                              (d/db-with [{:db/ident             :root
                                           :agg/id               1
                                           :user/login           "john"
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
                      {:effect   [:ui.form/edit (agg/allocate)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"
                                   :user/state    :archived}]}
                      {:throw (ex-info "Extra datoms"
                                       {:extra [(d/datom 1 :user/state :archived)]})}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))

(t/deftest process-already-logged-in
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {:current-user-id 1}}
                      {:final-effect [:ui.screen/show :main]}]
        continuation (e/continuation register/process)]
    (script/test continuation script)))
