(ns publicator.interactors.user.log-in
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(s/def ::params (s/keys :req-un [::user/login ::user/password]))

(defn- check-logged-out []
  (when (user-session/logged-in?)
    {:type ::already-logged-in}))

(defn- find-user [params]
  (user-q/get-by-login (:login params)))

(defn- check-authentication [user params]
  (when-not (and user (user/authenticated? user (:password params)))
    {:type ::authentication-failed}))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::params params)]
    {:type         ::invalid-params
     :explain-data exp}))

(b/defnc initial-params []
  :let [err (check-logged-out)]
  (some? err) err
  {:type ::initial-params
   :initial-params {}})

(b/defnc process [params]
  :let [err (or (check-logged-out)
                (check-params params))]
  (some? err) err
  :let [user (find-user params)
        err  (check-authentication user params)]
  (some? err) err
  :do (user-session/log-in! user)
  {:type ::processed :user user})
