(ns publicator.interactors.user.register
  (:require
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(s/def ::params ::user/build-params)

(defn initial-params [] {})

(defn- check-logged-out [ctx]
  (let [it (::session/session ctx)]
    (when (session/logged-in? it)
      {:type :already-logged-in})))

(defn- check-params [params]
  (when-let [exp (s/explain-data ::params params)]
    {:type         :invalid-params
     :explain-data exp}))

(defn- check-registered [ctx params]
  (let [it    (::user-q/get-by-login ctx)
        login (:login params)]
    (when (user-q/get-by-login it login)
      {:type :already-registered})))

(defn- create-user [ctx params]
  (let [it (::storage/storage ctx)]
    (storage/create-agg-in it (user/build params))))

(defn- log-in [ctx user]
  (let [it      (::session/session ctx)
        user-id (:id user)]
    (session/log-in! it user-id)))

(b/defnc call [ctx params]
  :let [err (or
             (check-logged-out ctx)
             (check-params params)
             (check-registered ctx params))]
  (some? err) [nil err]
  :let [user (create-user ctx params)]
  :do  (log-in ctx user)
  [user nil])
