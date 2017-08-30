(ns publicator.interactors.user.register
  (:require
   [publicator.interactors.abstractions.transaction :as tx]
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(defn- check-logged-out [{:keys [session]}]
  (when (session/read session :user-id)
    {:type :already-logged-in}))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::user/build-params params)]
    {:type         :invalid-params
     :explain-data exp}))

(defn- check-registered [{:keys [get-by-login-query]} {:keys [login]}]
  (when (user-q/get-by-login get-by-login-query login)
    {:type :already-registered}))

(defn- create-user [{:keys [tx-factory]} params]
  (tx/with-tx [tx (tx/build tx-factory)]
    (let [user-state (user/build params)
          user       (tx/create-aggregate tx user-state)]
      (:id @user))))

(defn- sign-in [{:keys [session]} user-id]
  (session/write! session :user-id user-id))

(b/defnc call [ctx params]
  :let [err (or
             (check-logged-out ctx)
             (check-params params)
             (check-registered ctx params))]
  (some? err) [nil err]
  :let [user-id (create-user ctx params)
        _ (sign-in ctx user-id)
        res {:user-id user-id}]
  [res nil])
