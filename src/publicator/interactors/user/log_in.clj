(ns publicator.interactors.user.log-in
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.abstractions.session :as session]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(s/def ::params (s/keys :req-un [::user/login ::user/password]))

(defn- check-logged-out [ctx]
  (let [it (::session/session ctx)]
    (when (session/logged-in? it)
      {:type ::already-logged-in})))

(defn- find-user [ctx params]
  (let [it    (::user-q/get-by-login ctx)
        login (:login params)]
    (user-q/get-by-login it login)))

(defn- check-authentication [user params]
  (when-not (user/authenticated? user (:password params))
    {:type ::authentication-failed}))

(defn- log-in [ctx user]
  (let [it (::session/session ctx)]
    (session/log-in! it (:id user))))

(defn check-params [params]
  (when-let [exp (s/explain-data ::params params)]
    {:type         ::invalid-params
     :explain-data exp}))

(defn check-ctx [ctx]
  (check-logged-out ctx))

(b/defnc call [ctx params]
  :let [err (or (check-ctx ctx)
                (check-params params))]
  (some? err) err
  :let [user (find-user ctx params)
        err  (check-authentication user params)]
  (some? err) err
  :do (log-in ctx user)
  {:type ::log-in :user user})
