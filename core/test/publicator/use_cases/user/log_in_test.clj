(ns publicator.use-cases.user.log-in-test
  (:require
   [publicator.use-cases.user.log-in :as log-in]
   [publicator.domain.aggregate :as agg]
   [publicator.util :as u]
   [clojure.test :as t]))

(t/deftest process-success
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {}}
                 {:effect   [:get-user-by-login "john"]
                  :coeffect (-> (agg/allocate :agg/user)
                                (agg/agg-with tx-data)
                                (agg/agg-with [{:db/ident             :root
                                                :agg/id               1
                                                :user/password-digest "digest"
                                                :user/state           :active}]))}
                 {:effect   [:check-password-digest "digest" "password"]
                  :coeffect true}
                 {:effect [:do
                           [:assoc-session :current-user-id 1]
                           [:show-screen :main]]}]]
    (u/test-with-script log-in/process script)))

(t/deftest process-not-found
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {}}
                 {:effect   [:get-user-by-login "john"]
                  :coeffect nil}
                 {:effect [:show-user-not-found-error]}]]
    (u/test-with-script log-in/process script)))

(t/deftest process-wrong-password
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "wrong-password"}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {}}
                 {:effect   [:get-user-by-login "john"]
                  :coeffect (-> (agg/allocate :agg/user)
                                (agg/agg-with tx-data)
                                (agg/agg-with [{:db/ident             :root
                                                :agg/id               1
                                                :user/password-digest "digest"
                                                :user/state           :active}]))}
                 {:effect   [:check-password-digest "digest" "wrong-password"]
                  :coeffect false}
                 {:effect [:show-user-not-found-error]}]]
    (u/test-with-script log-in/process script)))

(t/deftest process-additional-attrs
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"
                  :user/extra    :value}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {}}
                 {:effect [:show-additional-attributes-error #{:user/extra}]}]]
    (u/test-with-script log-in/process script)))

(t/deftest process-with-errr
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password ""}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {}}
                 {:effect [:show-validation-errors
                           #{{:error/type   :predicate
                              :error/entity 1
                              :error/attr   :user/password
                              :error/value  ""
                              :error/pred   ".{8,255}"
                              :error/rule   'root}}]}]]
    (u/test-with-script log-in/process script)))

(t/deftest process-already-logged-in
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {:current-user-id 1}}
                 {:effect [:show-screen :main]}]]
    (u/test-with-script log-in/process script)))
