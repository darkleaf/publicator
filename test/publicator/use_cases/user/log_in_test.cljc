(ns publicator.use-cases.user.log-in-test
  (:require
   [publicator.use-cases.user.log-in :as log-in]
   [publicator.domain.aggregate :as agg]
   [darkleaf.effect.core :as e]
   [clojure.test :as t]))

(t/deftest process-success
  (let [script         [{:args []}
                        {:effect   [:session/get]
                         :coeffect {}}
                        {:effect   [:ui/edit (agg/allocate :form.user/log-in)]
                         :coeffect [{:db/ident      :root
                                     :user/login    "john"
                                     :user/password "password"}]}
                        {:effect   [:persistence/user-by-login "john"]
                         :coeffect (-> (agg/allocate :agg/user)
                                       (agg/apply-tx [{:db/ident             :root
                                                       :agg/id               1
                                                       :user/login           "john"
                                                       :user/password-digest "digest"
                                                       :user/state           :active}]))}
                        {:effect   [:hasher/check "password" "digest"]
                         :coeffect true}
                        {:effect   [:session/assoc :current-user-id 1]
                         :coeffect nil}
                        {:final-effect [:ui/show-main-screen]}]
        continuation (e/continuation log-in/process)]
    (e/test continuation script)))

(t/deftest process-not-found
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui/edit (agg/allocate :form.user/log-in)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"}]}
                      {:effect   [:persistence/user-by-login "john"]
                       :coeffect nil}
                      {:final-effect [:ui/show-user-not-found-error]}]
        continuation (e/continuation log-in/process)]
    (e/test continuation script)))

(t/deftest process-wrong-password
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui/edit (agg/allocate :form.user/log-in)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "wrong-password"}]}
                      {:effect   [:persistence/user-by-login "john"]
                       :coeffect (-> (agg/allocate :agg/user)
                                     (agg/apply-tx [{:db/ident             :root
                                                     :agg/id               1
                                                     :user/login           "john"
                                                     :user/password-digest "digest"
                                                     :user/state           :active}]))}
                      {:effect   [:hasher/check "wrong-password" "digest"]
                       :coeffect false}
                      {:final-effect [:ui/show-user-not-found-error]}]
        continuation (e/continuation log-in/process)]
    (e/test continuation script)))

(t/deftest process-additional-attrs
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui/edit (agg/allocate :form.user/log-in)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password "password"
                                   :user/extra    :value}]}
                      {:final-effect [:ui/show-additional-attributes-error #{:user/extra}]}]
        continuation (e/continuation log-in/process)]
    (e/test continuation script)))

(t/deftest process-with-errr
  (let [script       [{:args []}
                      {:effect   [:session/get]
                       :coeffect {}}
                      {:effect   [:ui/edit (agg/allocate :form.user/log-in)]
                       :coeffect [{:db/ident      :root
                                   :user/login    "john"
                                   :user/password ""}]}
                      {:final-effect [:ui/show-validation-errors
                                      #{{:error/type   :predicate
                                         :error/entity 1
                                         :error/attr   :user/password
                                         :error/value  ""
                                         :error/pred   (str #".{8,255}")
                                         :error/rule   'root}}]}]
        continuation (e/continuation log-in/process)]
    (e/test continuation script)))

(t/deftest process-already-logged-in
  (let [script          [{:args []}
                         {:effect   [:session/get]
                          :coeffect {:current-user-id 1}}
                         {:final-effect [:ui/show-main-screen]}]
        continuation (e/continuation log-in/process)]
    (e/test continuation script)))
