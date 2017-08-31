(ns publicator.interactors.user.register
  (:require
   [publicator.interactors.abstractions.transaction :as tx]
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(defn- check-logged-out [ctx]
  (let [it (::session/session ctx)]
    (when (session/logged-in? it)
      {:type :already-logged-in})))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::user/build-params params)]
    {:type         :invalid-params
     :explain-data exp}))

(defn- check-registered [ctx params]
  (let [it    (::user-q/get-by-login ctx)
        login (:login params)]
    (when (user-q/get-by-login it login)
      {:type :already-registered})))

(defn- create-user [ctx params]
  (let [it (::tx/tx-factory ctx)]
    (tx/with-tx [tx (tx/build it)]
      (let [user-state (user/build params)
            user       (tx/create-aggregate tx user-state)]
        @user))))

(defn- log-in [ctx user]
  (let [it      (::session/session ctx)
        user-id (:id user)]
    (session/log-in! it user-id)))

(s/def ::ctx (s/keys :req [::session/session
                           ::tx/tx-factory
                           ::user-q/get-by-login]))

(b/defnc call [ctx params]
  :do  (s/assert ::ctx ctx)
  :let [err (or
             (check-logged-out ctx)
             (check-params params)
             (check-registered ctx params))]
  (some? err) [nil err]
  :let [user (create-user ctx params)]
  :do  (log-in ctx user)
  [user nil])
