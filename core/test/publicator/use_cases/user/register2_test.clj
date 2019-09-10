(ns publicator.use-cases.user.register2-test
  (:require
   [publicator.use-cases.user.register2 :as register]
   [publicator.domain.aggregates.user :as user]
   [publicator.domain.aggregate :as agg]
   [publicator.util :refer [linearize]]
   [clojure.core.match :as m]
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
  (let [msgs   [[:user/login :add :root "john"]
                [:user/password :add :root "password"]]
        script [{:coeffect msgs}
                {:effect   [:get-session]
                 :coeffect {}}
                {:effect   [:get-user-presence-by-login "john"]
                 :coeffect false}
                {:effect   [:get-password-digest "password"]
                 :coeffect "digest"}
                {:effect   [:get-new-user-id]
                 :coeffect 1}
                {:effect   [:do
                            [:assoc-session :current-user-id 1]
                            [:persist (-> user/new-blank
                                          (agg/with-msgs msgs)
                                          (agg/with-msgs
                                            [[:agg/id :add :root 1]
                                             [:user/password-digest :add :root "digest"]
                                             [:user/state :add :root :active]]))]
                            [:show-screen :main]]}]]
    (check-with-script register/process script)))

(t/deftest process-additional-msgs
  (let [script [{:coeffect [[:user/login :add :root "john"]
                            [:user/password :add :root "password"]
                            [:user/state :add :root :archived]]}
                {:effect [:show-additional-messages-error #{:user/state}]}]]
    (check-with-script register/process script)))

(t/deftest process-already-logged-in
  (let [script [{:coeffect [[:user/login :add :root "john"]
                            [:user/password :add :root "password"]]}
                {:effect   [:get-session]
                 :coeffect {:current-user-id 1}}
                {:effect [:show-screen :main]}]]
    (check-with-script register/process script)))

(t/deftest process-already-registered
  (let [script [{:coeffect [[:user/login :add :root "john"]
                            [:user/password :add :root "password"]]}

                {:effect   [:get-session]
                 :coeffect {}}
                {:effect   [:get-user-presence-by-login "john"]
                 :coeffect true}
                {:effect [:show-screen :main]}]]
    (check-with-script register/process script)))

(t/deftest process-with-errr
  (let [script [{:coeffect [[:user/login :add :root "john"]
                            [:user/password :add :root ""]]}
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
