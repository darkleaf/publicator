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
      {:type :already-logged-in})))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::params params)]
    {:type         :invalid-params
     :explain-data exp}))

(defn- find-user [ctx params]
  (let [it    (::user-q/get-by-login ctx)
        login (:login params)]
    (user-q/get-by-login it login)))

(defn- authenticated? [user params]
  (user/authenticated? user (:password params)))

(defn- log-in [ctx user]
  (let [it (::session/session ctx)]
    (session/log-in! it (:id user))))

(def ^:private error {:type :wrong-login-or-password})

(s/def ::ctx (s/keys :req [::session/session
                           ::user-q/get-by-login]))

(b/defnc call [ctx params]
  :do  (s/assert ::ctx ctx)
  :let [err (or (check-logged-out ctx)
                (check-params params))]
  (some? err) [nil err]
  :let [user (find-user ctx params)]
  (not (and user
            (authenticated? user params))) [nil error]
  :do (log-in ctx user)
  [user nil])
