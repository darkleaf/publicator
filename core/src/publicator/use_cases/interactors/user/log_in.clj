(ns publicator.use-cases.interactors.user.log-in
  (:require
   [publicator.use-cases.abstractions.user-queries :as user-q]
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.domain.aggregates.user :as user]
   [darkleaf.either :as e]
   [clojure.spec.alpha :as s]
   [publicator.utils.spec :as utils.spec]))

(s/def ::params (utils.spec/only-keys :req-un [::user/login ::user/password]))

(defn- check-authorization= []
  (if (user-session/logged-in?)
    (e/left [::already-logged-in])
    (e/right [::authorized])))

(defn- find-user= [params]
  (if-let [user (user-q/get-by-login (:login params))]
    (e/right user)
    (e/left [::authentication-failed])))

(defn- check-authentication= [user params]
  (if (user/authenticated? user (:password params))
    (e/right)
    (e/left [::authentication-failed])))

(defn- check-params= [params]
  (if-let [exp (s/explain-data ::params params)]
    (e/left [::invalid-params exp])
    (e/right)))

(defn initial-params []
  @(e/let= [ok (check-authorization=)]
     [::initial-params {}]))

(defn process [params]
  @(e/let= [ok   (check-authorization=)
            ok   (check-params= params)
            user (find-user= params)
            ok   (check-authentication= user params)]
     (user-session/log-in! user)
     [::processed]))

(defn authorize []
  @(check-authorization=))

(s/def ::already-logged-in (s/tuple #{::already-logged-in}))
(s/def ::authentication-failed (s/tuple #{::authentication-failed}))
(s/def ::invalid-params (s/tuple #{::invalid-params} map?))
(s/def ::initial-params (s/tuple #{::initial-params} map?))
(s/def ::processed (s/tuple #{::processed}))
(s/def ::authorized (s/tuple #{::authorized}))

(s/fdef initial-params
  :args nil?
  :ret (s/or :ok  ::initial-params
             :err ::already-logged-in))

(s/fdef process
  :args (s/cat :params any?)
  :ret (s/or :ok  ::processed
             :err ::already-logged-in
             :err ::authentication-failed
             :err ::invalid-params))

(s/fdef authorize
  :args nil?
  :ret (s/or :ok  ::authorized
             :err ::already-logged-in))
