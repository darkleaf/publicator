(ns publicator.use-cases.user.register-test
  (:require
   [publicator.use-cases.user.register :as register]
   [publicator.domain.aggregate :as agg]
   [clojure.test :as t]))

(defn- check-with-script [continuation script]
  (loop [[actual-effect continuation]       [nil continuation]
         [{:keys [effect coeffect]} & tail] script]
    (t/is (= effect actual-effect))
    (if (nil? continuation)
      (t/is (empty? tail))
      (recur (continuation coeffect)
             tail))))

(t/deftest process-success
  (let [tx-data [{:db/ident      :root
                  :user/login    "john"
                  :user/password "password"}]
        script  [{:coeffect tx-data}
                 {:effect   [:get-session]
                  :coeffect {}}
                 {:effect   [:get-user-presence-by-login "john"]
                  :coeffect false}
                 {:effect   [:get-password-digest "password"]
                  :coeffect "digest"}
                 {:effect   [:get-new-user-id]
                  :coeffect 1}
                 {:effect [:do
                           [:assoc-session :current-user-id 1]
                           [:persist (-> (agg/allocate :agg/new-user)
                                         (agg/agg-with tx-data)
                                         (agg/agg-with [{:db/ident             :root
                                                         :agg/id               1
                                                         :user/password-digest "digest"
                                                         :user/state           :active}]))]
                           [:show-screen :main]]}]]
    (check-with-script register/process script)))

(t/deftest process-additional-attrs
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"
                             :user/state    :archived}]}
                {:effect [:show-additional-attributes-error #{:user/state}]}]]
    (check-with-script register/process script)))

(t/deftest process-already-logged-in
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"}]}
                {:effect   [:get-session]
                 :coeffect {:current-user-id 1}}
                {:effect [:show-screen :main]}]]
    (check-with-script register/process script)))

(t/deftest process-already-registered
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password "password"}]}
                {:effect   [:get-session]
                 :coeffect {}}
                {:effect   [:get-user-presence-by-login "john"]
                 :coeffect true}
                {:effect [:show-screen :main]}]]
    (check-with-script register/process script)))

(t/deftest process-with-errr
  (let [script [{:coeffect [{:db/ident      :root
                             :user/login    "john"
                             :user/password ""}]}
                {:effect   [:get-session]
                 :coeffect {}}
                {:effect   [:get-user-presence-by-login "john"]
                 :coeffect false}
                {:effect   [:get-password-digest ""]
                 :coeffect "digest"}
                {:effect [:show-validation-errors #{{:error/type   :predicate
                                                     :error/entity 1
                                                     :error/attr   :user/password
                                                     :error/value  ""
                                                     :error/pred   ".{8,255}"
                                                     :error/rule   'root}}]}]]
    (check-with-script register/process script)))
