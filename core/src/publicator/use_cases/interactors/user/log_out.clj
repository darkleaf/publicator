(ns publicator.use-cases.interactors.user.log-out
  (:require
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.domain.aggregates.user :as user]
   [darkleaf.either :as e]
   [clojure.spec.alpha :as s]))

(defn- check-authorization= []
  (if (user-session/logged-out?)
    (e/left [::already-logged-out])
    (e/right [::authorized])))

(defn process []
  (e/extract
   (e/let= [ok (check-authorization=)]
     (user-session/log-out!)
     [::processed])))

(defn authorize []
  (e/extract
   (check-authorization=)))

(s/def ::already-logged-out (s/tuple #{::already-logged-out}))
(s/def ::processed (s/tuple #{::processed}))
(s/def ::authorized (s/tuple #{::authorized}))

(s/fdef process
  :args nil?
  :ret (s/or :ok  ::processed
             :err ::already-logged-out))

(s/fdef authorize
  :args nil?
  :ret (s/or :ok  ::authorized
             :err ::already-logged-out))
