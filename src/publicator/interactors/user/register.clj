(ns publicator.interactors.user.register
  (:require
   [publicator.interactors.abstractions.storage :as storage]
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(s/def ::params ::user/build-params)

(defn- check-logged-out []
  (when (user-session/logged-in?)
    {:type ::already-logged-in}))

(defn- check-registered [params]
  (when (user-q/get-by-login (:login params))
    {:type ::already-registered}))

(defn- create-user [params]
  (storage/tx-create (user/build params)))

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
  :let [err (or
             (check-logged-out)
             (check-params params)
             (check-registered params))]
  (some? err) err
  :let [user (create-user params)]
  :do  (user-session/log-in! user)
  {:type ::processed :user user})
