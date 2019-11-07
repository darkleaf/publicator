(ns publicator.use-cases.user.log-in-test
  (:require
   [publicator.use-cases.user.log-in :as log-in]
   [publicator.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [clojure.test :as t]))

(t/deftest process-success
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:effect   [:persistence/user-by-login "john"]
                  :coeffect (-> (agg/allocate :agg/user)
                                (agg/apply-tx tx-data)
                                (agg/apply-tx [{:db/ident             :root
                                                :agg/id               1
                                                :user/password-digest "digest"
                                                :user/state           :active}]))}
                 {:effect   [:hasher/check "password" "digest"]
                  :coeffect true}
                 {:effect   [:session/assoc :current-user-id 1]
                  :coeffect nil}
                 {:final-effect [:ui/show-main-screen]}]]
    (e/test log-in/process script)))

(t/deftest process-not-found
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:effect   [:persistence/user-by-login "john"]
                  :coeffect nil}
                 {:final-effect [:ui/show-user-not-found-error]}]]
    (e/test log-in/process script)))

(t/deftest process-wrong-password
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "wrong-password"}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:effect   [:persistence/user-by-login "john"]
                  :coeffect (-> (agg/allocate :agg/user)
                                (agg/apply-tx tx-data)
                                (agg/apply-tx [{:db/ident             :root
                                                :agg/id               1
                                                :user/password-digest "digest"
                                                :user/state           :active}]))}
                 {:effect   [:hasher/check "wrong-password" "digest"]
                  :coeffect false}
                 {:final-effect [:ui/show-user-not-found-error]}]]
    (e/test log-in/process script)))

(t/deftest process-additional-attrs
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"
                  :user/extra    :value}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:final-effect [:ui/show-additional-attributes-error #{:user/extra}]}]]
    (e/test log-in/process script)))

(t/deftest process-with-errr
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password ""}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {}}
                 {:final-effect [:ui/show-validation-errors
                                 #{{:error/type   :predicate
                                    :error/entity 1
                                    :error/attr   :user/password
                                    :error/value  ""
                                    :error/pred   ".{8,255}"
                                    :error/rule   'root}}]}]]
    (e/test log-in/process script)))

(t/deftest process-already-logged-in
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:args [tx-data]}
                 {:effect   [:session/get]
                  :coeffect {:current-user-id 1}}
                 {:final-effect [:ui/show-main-screen]}]]
    (e/test log-in/process script)))
