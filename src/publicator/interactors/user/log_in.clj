(ns publicator.interactors.user.log-in
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.helpers.user-session :as user-session]
   [publicator.domain.user :as user]
   [better-cond.core :as b]))

(defn- check-logged-out [ctx])

(defn- check-params [ctx])

;; get-by-login-query непонятно, что тут юзер

(defn- find-user [{:keys [get-by-login-query]} params]
  (user-q/get-by-login get-by-login-query (:login params)))

(defn- authenticated? [user params]
  (user/authenticated? user (:password params)))

(defn- log-in [{:keys [session]} user]
  (user-session/log-in session (:id user)))

(def error {:type :wrong-loggin-or-password})

(b/defnc call [ctx params]
  :let [err (or (check-logged-out ctx)
                (check-params params))]
  (some? err) [nil err]
  :let [user (find-user ctx params)]
  (not (and user
            (authenticated? user params))) [nil error]
  :do (log-in ctx user)
  [{:user user} nil])
